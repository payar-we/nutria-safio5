package com.busdedsafio.client;

import java.io.InputStreamReader;
import java.io.Reader;
import java.util.ArrayList;
import java.util.List;

import org.joml.Quaternionf;

import com.busdedsafio.BusMod;
import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import com.mojang.math.Axis;

import net.minecraft.client.Minecraft;
import net.minecraft.util.Mth;

/** Modelo del bus (bus_model.json, sacado del .bbmodel). Ruedas y helices giran. */
public class BusModel {
	private static BusModel INSTANCE;
	private static boolean failed;

	private static final String[] FACES = { "north", "south", "east", "west", "up", "down" };
	private static final float[][] NORMALS = { { 0, 0, -1 }, { 0, 0, 1 }, { 1, 0, 0 }, { -1, 0, 0 }, { 0, 1, 0 }, { 0, -1, 0 } };

	private static class Cube {
		float[] from, to, origin, rot;
		float[][] uv = new float[6][];
	}

	private static class Part {
		boolean wheel;
		boolean axisX;
		float[] pivot;
		List<Cube> cubes = new ArrayList<>();
	}

	private final List<Cube> fixed = new ArrayList<>();
	private final List<Part> parts = new ArrayList<>();
	private float texW = 512, texH = 512;

	public static BusModel get() {
		if (INSTANCE == null && !failed) {
			try {
				INSTANCE = load();
			} catch (Throwable t) {
				failed = true;
			}
		}
		return INSTANCE;
	}

	private static float[] arr(JsonElement e) {
		JsonArray a = e.getAsJsonArray();
		float[] r = new float[a.size()];
		for (int i = 0; i < r.length; i++) r[i] = a.get(i).getAsFloat();
		return r;
	}

	private static Cube cube(JsonObject o) {
		Cube c = new Cube();
		c.from = arr(o.get("from"));
		c.to = arr(o.get("to"));
		c.origin = arr(o.get("origin"));
		c.rot = arr(o.get("rotation"));
		JsonObject faces = o.getAsJsonObject("faces");
		for (int i = 0; i < 6; i++) {
			if (faces.has(FACES[i])) c.uv[i] = arr(faces.get(FACES[i]));
		}
		return c;
	}

	private static BusModel load() throws Exception {
		var res = Minecraft.getInstance().getResourceManager().getResource(BusMod.id("bus_model.json"));
		if (res.isEmpty()) throw new IllegalStateException("falta bus_model.json");
		BusModel m = new BusModel();
		try (Reader r = new InputStreamReader(res.get().open())) {
			JsonObject root = JsonParser.parseReader(r).getAsJsonObject();
			m.texW = root.get("texW").getAsFloat();
			m.texH = root.get("texH").getAsFloat();
			for (JsonElement e : root.getAsJsonArray("static")) m.fixed.add(cube(e.getAsJsonObject()));
			for (var entry : root.getAsJsonObject("parts").entrySet()) {
				JsonObject po = entry.getValue().getAsJsonObject();
				Part p = new Part();
				p.wheel = entry.getKey().startsWith("wheel");
				p.axisX = po.get("axis").getAsString().equals("x");
				p.pivot = arr(po.get("pivot"));
				for (JsonElement e : po.getAsJsonArray("cubes")) p.cubes.add(cube(e.getAsJsonObject()));
				m.parts.add(p);
			}
		}
		return m;
	}

	/** El PoseStack ya debe tener la escala 1/16. */
	public void render(PoseStack pose, VertexConsumer vc, int light, int overlay, float wheelAngle, float rotorAngle) {
		for (Cube c : fixed) renderCube(c, pose, vc, light, overlay);
		for (Part p : parts) {
			pose.pushPose();
			pose.translate(p.pivot[0], p.pivot[1], p.pivot[2]);
			float angle = p.wheel ? wheelAngle : rotorAngle;
			pose.mulPose((p.axisX ? Axis.XP : Axis.YP).rotationDegrees(angle));
			pose.translate(-p.pivot[0], -p.pivot[1], -p.pivot[2]);
			for (Cube c : p.cubes) renderCube(c, pose, vc, light, overlay);
			pose.popPose();
		}
	}

	private void renderCube(Cube c, PoseStack pose, VertexConsumer vc, int light, int overlay) {
		pose.pushPose();
		pose.translate(c.origin[0], c.origin[1], c.origin[2]);
		pose.mulPose(new Quaternionf().rotationZYX(c.rot[2] * Mth.DEG_TO_RAD, c.rot[1] * Mth.DEG_TO_RAD, c.rot[0] * Mth.DEG_TO_RAD));
		pose.translate(-c.origin[0], -c.origin[1], -c.origin[2]);
		float x1 = c.from[0], y1 = c.from[1], z1 = c.from[2], x2 = c.to[0], y2 = c.to[1], z2 = c.to[2];
		var last = pose.last();
		for (int f = 0; f < 6; f++) {
			float[] uv = c.uv[f];
			if (uv == null) continue;
			float[][] v = corners(f, x1, y1, z1, x2, y2, z2);
			float[][] t = { { uv[0], uv[1] }, { uv[2], uv[1] }, { uv[2], uv[3] }, { uv[0], uv[3] } };
			float[] n = NORMALS[f];
			for (int i = 0; i < 4; i++) {
				vc.addVertex(last, v[i][0], v[i][1], v[i][2])
						.setColor(-1)
						.setUv(t[i][0] / texW, t[i][1] / texH)
						.setOverlay(overlay)
						.setLight(light)
						.setNormal(last, n[0], n[1], n[2]);
			}
		}
		pose.popPose();
	}

	private static float[][] corners(int face, float x1, float y1, float z1, float x2, float y2, float z2) {
		return switch (face) {
			case 0 -> new float[][] { { x2, y2, z1 }, { x1, y2, z1 }, { x1, y1, z1 }, { x2, y1, z1 } };
			case 1 -> new float[][] { { x1, y2, z2 }, { x2, y2, z2 }, { x2, y1, z2 }, { x1, y1, z2 } };
			case 2 -> new float[][] { { x2, y2, z2 }, { x2, y2, z1 }, { x2, y1, z1 }, { x2, y1, z2 } };
			case 3 -> new float[][] { { x1, y2, z1 }, { x1, y2, z2 }, { x1, y1, z2 }, { x1, y1, z1 } };
			case 4 -> new float[][] { { x1, y2, z1 }, { x2, y2, z1 }, { x2, y2, z2 }, { x1, y2, z2 } };
			default -> new float[][] { { x1, y1, z2 }, { x2, y1, z2 }, { x2, y1, z1 }, { x1, y1, z1 } };
		};
	}
}
