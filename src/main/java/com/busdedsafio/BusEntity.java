package com.busdedsafio;

import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.network.syncher.EntityDataAccessor;
import net.minecraft.network.syncher.EntityDataSerializers;
import net.minecraft.network.syncher.SynchedEntityData;
import net.minecraft.util.Mth;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.MoverType;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;

/**
 * Bus volador. 1 conductor (ticket rojo) + 6 pasajeros atras (ticket amarillo).
 * El movimiento lo calcula el cliente del conductor (como los botes de vanilla).
 */
public class BusEntity extends Entity {
	private static final EntityDataAccessor<Boolean> FLYING = SynchedEntityData.defineId(BusEntity.class, EntityDataSerializers.BOOLEAN);
	private static final EntityDataAccessor<Integer> DRIVER = SynchedEntityData.defineId(BusEntity.class, EntityDataSerializers.INT);

	// Asientos en pixeles del modelo: x (+ = derecha), z (+ = hacia atras)
	private static final float DRIVER_X = -5.0F, DRIVER_Z = 5.5F, DRIVER_FLOOR = 14.0F;
	private static final float[][] BACK = { { -10, 20 }, { 10, 20 }, { -10, 31 }, { 10, 31 }, { -10, 42 }, { 10, 42 } };
	private static final float BACK_FLOOR = 15.0F;
	private static final float CENTER_Z = 13.5F, GROUND_Y = 4.0F;
	public static final int MAX_BACK = 6;

	// animacion (solo cliente)
	public float wheelAngle, prevWheelAngle, rotorAngle, prevRotorAngle, rotorSpeed;

	public BusEntity(EntityType<? extends BusEntity> type, Level level) {
		super(type, level);
	}

	@Override
	protected void defineSynchedData(SynchedEntityData.Builder builder) {
		builder.define(FLYING, false);
		builder.define(DRIVER, -1);
	}

	@Override
	protected void readAdditionalSaveData(CompoundTag tag) {
	}

	@Override
	protected void addAdditionalSaveData(CompoundTag tag) {
	}

	public boolean isFlying() {
		return this.entityData.get(FLYING);
	}

	public void setFlying(boolean flying) {
		this.entityData.set(FLYING, flying);
	}

	// ---------- pasajeros ----------

	private Entity driverEntity() {
		int id = this.entityData.get(DRIVER);
		if (id < 0) return null;
		Entity e = this.level().getEntity(id);
		return (e != null && this.hasPassenger(e)) ? e : null;
	}

	@Override
	public LivingEntity getControllingPassenger() {
		return driverEntity() instanceof LivingEntity living ? living : null;
	}

	@Override
	protected boolean canAddPassenger(Entity passenger) {
		return this.getPassengers().size() < MAX_BACK + 1;
	}

	@Override
	protected void removePassenger(Entity passenger) {
		super.removePassenger(passenger);
		if (!this.level().isClientSide && passenger.getId() == this.entityData.get(DRIVER)) {
			this.entityData.set(DRIVER, -1);
		}
	}

	private Vec3 seatOffset(Entity passenger) {
		Entity driver = driverEntity();
		double sx, sz, floor;
		if (passenger == driver) {
			sx = DRIVER_X;
			sz = DRIVER_Z;
			floor = DRIVER_FLOOR;
		} else {
			int idx = 0;
			for (Entity e : this.getPassengers()) {
				if (e == passenger) break;
				if (e != driver) idx++;
			}
			idx = Math.min(idx, MAX_BACK - 1);
			sx = BACK[idx][0];
			sz = BACK[idx][1];
			floor = BACK_FLOOR;
		}
		double right = sx / 16.0;
		double fwd = -(sz - CENTER_Z) / 16.0;
		double y = (floor - GROUND_Y) / 16.0 - 0.40;
		float r = this.getYRot() * Mth.DEG_TO_RAD;
		double fx = -Mth.sin(r), fz = Mth.cos(r);
		double rx = -Mth.cos(r), rz = -Mth.sin(r);
		return new Vec3(rx * right + fx * fwd, y, rz * right + fz * fwd);
	}

	@Override
	protected void positionRider(Entity passenger, Entity.MoveFunction callback) {
		if (!this.hasPassenger(passenger)) return;
		Vec3 o = seatOffset(passenger);
		callback.accept(passenger, this.getX() + o.x, this.getY() + o.y, this.getZ() + o.z);
	}

	@Override
	public Vec3 getDismountLocationForPassenger(LivingEntity passenger) {
		float r = this.getYRot() * Mth.DEG_TO_RAD;
		return this.position().add(-Mth.cos(r) * 2.2, 0.0, -Mth.sin(r) * 2.2);
	}

	@Override
	public InteractionResult interact(Player player, InteractionHand hand) {
		if (player.isSecondaryUseActive()) return InteractionResult.PASS;
		if (this.level().isClientSide) return InteractionResult.SUCCESS;

		ItemStack held = player.getItemInHand(hand);
		if (held.is(ModItems.TICKET_ROJO)) {
			if (driverEntity() != null) {
				player.displayClientMessage(Component.translatable("message.busdedsafio.driver_taken"), true);
			} else if (player.startRiding(this)) {
				this.entityData.set(DRIVER, player.getId());
			}
		} else if (held.is(ModItems.TICKET_AMARILLO)) {
			int back = this.getPassengers().size() - (driverEntity() != null ? 1 : 0);
			if (back >= MAX_BACK) {
				player.displayClientMessage(Component.translatable("message.busdedsafio.back_full"), true);
			} else {
				player.startRiding(this);
			}
		} else {
			player.displayClientMessage(Component.translatable("message.busdedsafio.need_ticket"), true);
		}
		return InteractionResult.CONSUME;
	}

	@Override
	public boolean hurt(DamageSource source, float amount) {
		if (this.isRemoved() || this.isInvulnerableTo(source)) return false;
		if (!this.level().isClientSide && source.getEntity() instanceof Player player && player.isShiftKeyDown()) {
			if (!player.getAbilities().instabuild) {
				this.spawnAtLocation(ModItems.BUS_ITEM);
			}
			this.ejectPassengers();
			this.discard();
			return true;
		}
		return false;
	}

	@Override
	public boolean isPickable() {
		return !this.isRemoved();
	}

	@Override
	public boolean canBeCollidedWith() {
		return true;
	}

	@Override
	public ItemStack getPickResult() {
		return new ItemStack(ModItems.BUS_ITEM);
	}

	@Override
	public float maxUpStep() {
		return 1.0F;
	}

	@Override
	public AABB getBoundingBoxForCulling() {
		return this.getBoundingBox().inflate(3.0, 2.5, 3.0);
	}

	// ---------- movimiento ----------

	@Override
	public void tick() {
		super.tick();
		LivingEntity driver = this.getControllingPassenger();
		if (this.isControlledByLocalInstance() && driver != null) {
			drive(driver, this.isFlying());
			this.move(MoverType.SELF, this.getDeltaMovement());
		} else if (!this.level().isClientSide && driver == null) {
			if (this.isFlying()) this.setFlying(false);
			Vec3 v = this.getDeltaMovement();
			double vy = this.onGround() ? -0.04 : (v.y - 0.04) * 0.98;
			this.setDeltaMovement(v.x * 0.6, vy, v.z * 0.6);
			this.move(MoverType.SELF, this.getDeltaMovement());
		}
		if (this.level().isClientSide) {
			updateAnimation();
		}
	}

	private void drive(LivingEntity driver, boolean flying) {
		float yaw = this.getYRot();
		float fwdIn = BusInput.forward, sideIn = BusInput.side;
		Vec3 v = this.getDeltaMovement();

		if (flying) {
			// vuelo estilo creativo: el bus mira hacia donde mira el conductor
			yaw = Mth.approachDegrees(yaw, driver.getYRot(), 5.0F);
			float r = yaw * Mth.DEG_TO_RAD;
			double fx = -Mth.sin(r), fz = Mth.cos(r);
			double lx = Mth.cos(r), lz = Mth.sin(r);
			double tx = (fx * fwdIn + lx * sideIn) * 0.5;
			double tz = (fz * fwdIn + lz * sideIn) * 0.5;
			double ty = ((BusInput.up ? 1 : 0) - (BusInput.down ? 1 : 0)) * 0.35;
			v = new Vec3(v.x + (tx - v.x) * 0.12, v.y + (ty - v.y) * 0.2, v.z + (tz - v.z) * 0.12);
		} else {
			float r = yaw * Mth.DEG_TO_RAD;
			double fx = -Mth.sin(r), fz = Mth.cos(r);
			double s = v.x * fx + v.z * fz;
			double target = fwdIn > 0 ? fwdIn * 0.5 : fwdIn * 0.2;
			s += (target - s) * (fwdIn == 0 ? 0.05 : 0.09);
			float turn = sideIn * 3.0F * Mth.clamp((float) Math.abs(s) / 0.25F, 0.3F, 1.0F) * (s < -0.02 ? -1.0F : 1.0F);
			yaw -= turn;
			r = yaw * Mth.DEG_TO_RAD;
			fx = -Mth.sin(r);
			fz = Mth.cos(r);
			double vy = this.onGround() ? -0.05 : (v.y - 0.08) * 0.98;
			v = new Vec3(fx * s, vy, fz * s);
		}
		this.setYRot(yaw);
		this.setDeltaMovement(v);
	}

	private void updateAnimation() {
		prevWheelAngle = wheelAngle;
		prevRotorAngle = rotorAngle;
		double dx = this.getX() - this.xo, dz = this.getZ() - this.zo;
		float r = this.getYRot() * Mth.DEG_TO_RAD;
		double forward = dx * (-Mth.sin(r)) + dz * Mth.cos(r);
		if (!this.isFlying()) {
			wheelAngle += (float) (-forward * 16.0 / 5.5 * 57.29578);
		}
		float target = this.isFlying() ? 70.0F : 0.0F;
		rotorSpeed += (target - rotorSpeed) * 0.08F;
		rotorAngle += rotorSpeed;
	}
}
