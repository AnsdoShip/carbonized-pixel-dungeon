/*
 * Pixel Dungeon
 * Copyright (C) 2012-2015 Oleg Dolya
 *
 * Shattered Pixel Dungeon
 * Copyright (C) 2014-2021 Evan Debenham
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>
 */

package com.ansdoship.carbonizedpixeldungeon.levels.traps;

import com.ansdoship.carbonizedpixeldungeon.Dungeon;
import com.ansdoship.carbonizedpixeldungeon.actors.Actor;
import com.ansdoship.carbonizedpixeldungeon.actors.Char;
import com.ansdoship.carbonizedpixeldungeon.actors.buffs.Buff;
import com.ansdoship.carbonizedpixeldungeon.actors.buffs.FlavourBuff;
import com.ansdoship.carbonizedpixeldungeon.actors.mobs.Mob;
import com.ansdoship.carbonizedpixeldungeon.effects.CellEmitter;
import com.ansdoship.carbonizedpixeldungeon.effects.particles.PitfallParticle;
import com.ansdoship.carbonizedpixeldungeon.items.Heap;
import com.ansdoship.carbonizedpixeldungeon.items.Item;
import com.ansdoship.carbonizedpixeldungeon.levels.features.Chasm;
import com.ansdoship.carbonizedpixeldungeon.messages.Messages;
import com.ansdoship.carbonizedpixeldungeon.scenes.GameScene;
import com.ansdoship.carbonizedpixeldungeon.utils.GLog;
import com.ansdoship.pixeldungeonclasses.utils.Bundle;
import com.ansdoship.pixeldungeonclasses.utils.PathFinder;

public class PitfallTrap extends Trap {

	{
		color = RED;
		shape = DIAMOND;
	}

	@Override
	public void activate() {
		
		if( Dungeon.bossLevel() || Dungeon.depth > 25){
			GLog.w(Messages.get(this, "no_pit"));
			return;
		}

		DelayedPit p = Buff.affect(Dungeon.hero, DelayedPit.class, 1);
		p.depth = Dungeon.depth;
		p.pos = pos;

		for (int i : PathFinder.NEIGHBOURS9){
			if (!Dungeon.level.solid[pos+i] || Dungeon.level.passable[pos+i]){
				CellEmitter.floor(pos+i).burst(PitfallParticle.FACTORY4, 8);
			}
		}

		if (pos == Dungeon.hero.pos){
			GLog.n(Messages.get(this, "triggered_hero"));
		} else if (Dungeon.level.heroFOV[pos]){
			GLog.n(Messages.get(this, "triggered"));
		}

	}

	public static class DelayedPit extends FlavourBuff {

		{
			revivePersists = true;
		}

		int pos;
		int depth;

		@Override
		public boolean act() {

			boolean herofell = false;
			if (depth == Dungeon.depth) {
				for (int i : PathFinder.NEIGHBOURS9) {

					int cell = pos + i;

					if (Dungeon.level.solid[pos+i] && !Dungeon.level.passable[pos+i]){
						continue;
					}

					CellEmitter.floor(pos+i).burst(PitfallParticle.FACTORY8, 12);

					Heap heap = Dungeon.level.heaps.get(cell);

					if (heap != null && heap.type != Heap.Type.FOR_SALE
							&& heap.type != Heap.Type.LOCKED_CHEST
							&& heap.type != Heap.Type.CRYSTAL_CHEST) {
						for (Item item : heap.items) {
							Dungeon.dropToChasm(item);
						}
						heap.sprite.kill();
						GameScene.discard(heap);
						Dungeon.level.heaps.remove(cell);
					}

					Char ch = Actor.findChar(cell);

					//don't trigger on flying chars, or immovable neutral chars
					if (ch != null && !ch.flying
						&& !(ch.alignment == Char.Alignment.NEUTRAL && Char.hasProp(ch, Char.Property.IMMOVABLE))) {
						if (ch == Dungeon.hero) {
							Chasm.heroFall(cell);
							herofell = true;
						} else {
							Chasm.mobFall((Mob) ch);
						}
					}

				}
			}

			detach();
			return !herofell;
		}

		private static final String POS = "pos";
		private static final String DEPTH = "depth";

		@Override
		public void storeInBundle(Bundle bundle) {
			super.storeInBundle(bundle);
			bundle.put(POS, pos);
			bundle.put(DEPTH, depth);
		}

		@Override
		public void restoreFromBundle(Bundle bundle) {
			super.restoreFromBundle(bundle);
			pos = bundle.getInt(POS);
			depth = bundle.getInt(DEPTH);
		}

	}
}