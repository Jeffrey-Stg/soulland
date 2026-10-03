package com.zelf115.soulland.cultivation.skill;

import com.zelf115.soulland.DerivedStats;
import com.zelf115.soulland.Stats;
import net.minecraft.world.entity.player.Player;

/** One firing of a skill by one player. */
public record SkillCast(Player caster, Skill skill) {

    public double scaledBySpirit(final double value) {
        return DerivedStats.scaledBySpirit(value, Stats.getSpirit(caster));
    }
}
