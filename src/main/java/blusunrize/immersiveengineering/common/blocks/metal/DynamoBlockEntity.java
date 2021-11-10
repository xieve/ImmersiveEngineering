/*
 * BluSunrize
 * Copyright (c) 2017
 *
 * This code is licensed under "Blu's License of Common Sense"
 * Details can be found in the license file in the root folder of this project
 */

package blusunrize.immersiveengineering.common.blocks.metal;

import blusunrize.immersiveengineering.api.IEEnums.IOSideConfig;
import blusunrize.immersiveengineering.api.IEProperties;
import blusunrize.immersiveengineering.api.energy.IRotationAcceptor;
import blusunrize.immersiveengineering.common.blocks.IEBaseBlockEntity;
import blusunrize.immersiveengineering.common.blocks.IEBlockInterfaces.IStateBasedDirectional;
import blusunrize.immersiveengineering.common.config.IEServerConfig;
import blusunrize.immersiveengineering.common.register.IEBlockEntities;
import blusunrize.immersiveengineering.common.util.DirectionUtils;
import blusunrize.immersiveengineering.common.util.EnergyHelper;
import blusunrize.immersiveengineering.common.util.EnergyHelper.IEForgeEnergyWrapper;
import blusunrize.immersiveengineering.common.util.EnergyHelper.IIEInternalFluxConnector;
import blusunrize.immersiveengineering.common.util.Utils;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.properties.Property;
import net.minecraft.world.phys.Vec3;
import net.minecraftforge.common.capabilities.Capability;
import net.minecraftforge.common.util.LazyOptional;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

public class DynamoBlockEntity extends IEBaseBlockEntity implements IIEInternalFluxConnector, IStateBasedDirectional
{
	public DynamoBlockEntity(BlockPos pos, BlockState state)
	{
		super(IEBlockEntities.DYNAMO.get(), pos, state);
	}

	@Override
	public Property<Direction> getFacingProperty()
	{
		return IEProperties.FACING_HORIZONTAL;
	}

	@Override
	public PlacementLimitation getFacingLimitation()
	{
		return PlacementLimitation.HORIZONTAL;
	}

	@Override
	public boolean mirrorFacingOnPlacement(LivingEntity placer)
	{
		return true;
	}

	@Override
	public boolean canHammerRotate(Direction side, Vec3 hit, LivingEntity entity)
	{
		return true;
	}

	@Override
	public boolean canRotate(Direction axis)
	{
		return true;
	}

	@Override
	public void readCustomNBT(CompoundTag nbt, boolean descPacket)
	{
	}

	@Override
	public void writeCustomNBT(CompoundTag nbt, boolean descPacket)
	{
	}

	@Nonnull
	@Override
	public IOSideConfig getEnergySideConfig(@Nullable Direction facing)
	{
		return IOSideConfig.OUTPUT;
	}

	@Override
	public boolean canConnectEnergy(Direction from)
	{
		return true;
	}

	IEForgeEnergyWrapper wrapper = new IEForgeEnergyWrapper(this, null);

	@Override
	public IEForgeEnergyWrapper getCapabilityWrapper(Direction facing)
	{
		return wrapper;
	}

	private final LazyOptional<IRotationAcceptor> rotationCap = registerConstantCap(new RotationAcceptor());

	@Nonnull
	@Override
	public <T> LazyOptional<T> getCapability(@Nonnull Capability<T> cap, @Nullable Direction side)
	{
		if(cap==IRotationAcceptor.CAPABILITY&&side==getFacing())
			return rotationCap.cast();
		return super.getCapability(cap, side);
	}

	private class RotationAcceptor implements IRotationAcceptor
	{
		@Override
		public void inputRotation(double rotation)
		{
			int output = (int)(IEServerConfig.MACHINES.dynamo_output.get()*rotation);
			for(Direction fd : DirectionUtils.VALUES)
			{
				BlockPos outputPos = getBlockPos().relative(fd);
				BlockEntity te = Utils.getExistingTileEntity(level, outputPos);
				output -= EnergyHelper.insertFlux(te, fd.getOpposite(), output, false);
			}
		}
	}
}