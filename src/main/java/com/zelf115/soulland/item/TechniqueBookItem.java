package com.zelf115.soulland.item;

import com.zelf115.soulland.cultivation.CultivationAttachment;
import com.zelf115.soulland.cultivation.technique.LearnedTechniques;
import com.zelf115.soulland.cultivation.technique.Technique;
import java.util.List;
import net.minecraft.ChatFormatting;
import net.minecraft.network.chat.Component;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResultHolder;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.level.Level;

/** A book found in structure chests; reading it teaches its technique once. */
public class TechniqueBookItem extends Item {

    private final Technique technique;

    public TechniqueBookItem(final Technique technique, final Properties properties) {
        super(properties);
        this.technique = technique;
    }

    @Override
    public InteractionResultHolder<ItemStack> use(final Level level, final Player player, final InteractionHand hand) {
        final ItemStack stack = player.getItemInHand(hand);
        if (level.isClientSide()) {
            return InteractionResultHolder.sidedSuccess(stack, true);
        }

        final LearnedTechniques techniques = player.getData(CultivationAttachment.CULTIVATION_DATA.get()).getTechniques();
        if (techniques.isLearned(technique)) {
            player.sendSystemMessage(Component.translatable("soulland.technique.already_known", technique.displayName()));
            return InteractionResultHolder.fail(stack);
        }

        techniques.learn(technique);
        player.sendSystemMessage(Component.translatable("soulland.technique.learned", technique.displayName()));
        if (!player.getAbilities().instabuild) {
            stack.shrink(1);
        }
        return InteractionResultHolder.sidedSuccess(stack, false);
    }

    @Override
    public boolean isFoil(final ItemStack stack) {
        return true;
    }

    @Override
    public void appendHoverText(final ItemStack stack, final TooltipContext context, final List<Component> tooltipComponents,
                                final TooltipFlag tooltipFlag) {
        tooltipComponents.add(technique.displayName().copy().withStyle(ChatFormatting.LIGHT_PURPLE));
    }
}
