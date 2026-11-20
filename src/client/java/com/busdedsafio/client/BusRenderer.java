package com.busdedsafio.client;

import com.busdedsafio.BusEntity;
import com.busdedsafio.BusMod;
import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import com.mojang.math.Axis;

import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.entity.EntityRenderer;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.client.renderer.texture.OverlayTexture;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.Mth;

public class BusRenderer extends EntityRenderer<BusEntity> {
	private static final ResourceLocation TEXTURE = BusMod.id("textures/entity/bus.png");

	public BusRenderer(EntityRendererProvider.Context context) {
		super(context);
		this.shadowRadius = 1.5F;
	}

	@Override
	public ResourceLocation getTextureLocation(BusEntity entity) {
		return TEXTURE;
	}

	@Override
	public void render(BusEntity bus, float entityYaw, float partialTick, PoseStack pose, MultiBufferSource buffers, int light) {
		BusModel model = BusModel.get();
		if (model == null) return;
		float yaw = Mth.rotLerp(partialTick, bus.yRotO, bus.getYRot());
		pose.pushPose();
		pose.mulPose(Axis.YP.rotationDegrees(180.0F - yaw));
		pose.scale(1.0F / 16.0F, 1.0F / 16.0F, 1.0F / 16.0F);
		pose.translate(0.0F, -4.0F, -13.5F); // centra el modelo y apoya las ruedas en el suelo
		VertexConsumer vc = buffers.getBuffer(RenderType.entityCutoutNoCull(TEXTURE));
		model.render(pose, vc, light, OverlayTexture.NO_OVERLAY,
				Mth.lerp(partialTick, bus.prevWheelAngle, bus.wheelAngle),
				Mth.lerp(partialTick, bus.prevRotorAngle, bus.rotorAngle));
		pose.popPose();
	}
}
