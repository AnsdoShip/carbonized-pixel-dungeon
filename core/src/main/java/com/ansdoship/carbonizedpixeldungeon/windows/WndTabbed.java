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

package com.ansdoship.carbonizedpixeldungeon.windows;

import com.ansdoship.carbonizedpixeldungeon.Assets;
import com.ansdoship.carbonizedpixeldungeon.Chrome;
import com.ansdoship.carbonizedpixeldungeon.scenes.PixelScene;
import com.ansdoship.carbonizedpixeldungeon.ui.RenderedTextBlock;
import com.ansdoship.carbonizedpixeldungeon.ui.Window;
import com.ansdoship.pixeldungeonclasses.noosa.Game;
import com.ansdoship.pixeldungeonclasses.noosa.Image;
import com.ansdoship.pixeldungeonclasses.noosa.NinePatch;
import com.ansdoship.pixeldungeonclasses.noosa.PointerArea;
import com.ansdoship.pixeldungeonclasses.noosa.audio.Sample;
import com.ansdoship.pixeldungeonclasses.noosa.ui.Button;
import com.ansdoship.pixeldungeonclasses.noosa.ui.Component;
import com.ansdoship.pixeldungeonclasses.utils.RectF;

import java.util.ArrayList;

public class WndTabbed extends Window {

	protected ArrayList<Tab> tabs = new ArrayList<>();
	protected Tab selected;
	
	public WndTabbed() {
		super( 0, 0, Chrome.get( Chrome.Type.TOAST ) );
	}
	
	protected Tab add( Tab tab ) {

		tab.setPos( tabs.size() == 0 ?
			-chrome.marginLeft() :
			tabs.get( tabs.size() - 1 ).right(), height + 1 );
		tab.select( tab.selected );
		super.addToBack( tab );
		
		tabs.add( tab );

		return tab;
	}
	
	public void select( int index ) {
		select( tabs.get( index ) );
	}
	
	public void select( Tab tab ) {
		if (tab != selected) {
			for (Tab t : tabs) {
				if (t == selected) {
					t.select( false );
				} else if (t == tab) {
					t.select( true );
				}
			}
			
			selected = tab;
		}
	}
	
	@Override
	public void resize( int w, int h ) {
		// -> super.resize(...)
		this.width = w;
		this.height = h;
		
		chrome.size(
			width + chrome.marginHor(),
			height + chrome.marginVer() );
		
		camera.resize( (int)chrome.width, chrome.marginTop() + height + 1 + tabHeight() + 4);
		camera.x = (int)(Game.width - camera.screenWidth()) / 2;
		camera.y = (int)(Game.height - camera.screenHeight()) / 2;
		camera.y += yOffset * camera.zoom;

		/*
		shadow.boxRect(
				camera.x / camera.zoom,
				camera.y / camera.zoom,
				chrome.width(), chrome.height );
		 */
		// <- super.resize(...)
		
		for (Tab tab : tabs) {
			remove( tab );
		}
		
		ArrayList<Tab> tabs = new ArrayList<>(this.tabs);
		this.tabs.clear();
		
		for (Tab tab : tabs) {
			add( tab );
		}
	}

	public void layoutTabs(){
		//subtract two as that horizontal space is transparent at the bottom
		int fullWidth = width+chrome.marginHor();
		float numTabs = tabs.size();
		float tabWidth = (fullWidth - (numTabs-1))/numTabs;

		float pos = -chrome.marginLeft();
		for (Tab tab : tabs){
			tab.setSize(tabWidth, tabHeight());
			tab.setPos(pos, height + 1);
			pos = tab.right() + 1;
			PixelScene.align(tab);
		}
	}
	
	protected int tabHeight() {
		return 25 - 4;
	}
	
	protected void onClick( Tab tab ) {
		select( tab );
	}
	
	protected class Tab extends Button {

		protected float selectedHeight;
		protected float normalHeight;

		{
			hotArea.blockLevel = PointerArea.ALWAYS_BLOCK;
		}

		@Override
		public Component setSize(float width, float height) {
			selectedHeight = height + 4;
			normalHeight = height;
			return super.setSize(width, height);
		}

		@Override
		public Component setRect(float x, float y, float width, float height) {
			selectedHeight = height + 4;
			normalHeight = height;
			return super.setRect(x, y, width, height);
		}

		protected final int CUT = 5;
		
		protected boolean selected;
		
		protected NinePatch bg;
		
		@Override
		protected void layout() {
			height = selected ? selectedHeight : normalHeight;
			super.layout();
			
			if (bg != null) {
				bg.x = x;
				bg.y = y;
				bg.size( width, height );
			}
		}
		
		protected void select( boolean value ) {
			
			active = !(selected = value);

			if (!active) killTooltip();
			
			if (bg != null) {
				remove( bg );
			}
			
			bg = Chrome.get( Chrome.Type.TOAST );
			addToBack( bg );
			
			layout();
		}

		@Override
		protected void onClick() {
			WndTabbed.this.onClick( this );
		}

		@Override
		protected void onPointerDown() {
			bg.brightness( 1.2f );
			Sample.INSTANCE.play( Assets.Sounds.CLICK, 0.7f, 0.7f, 1.2f );
		}

		@Override
		protected void onPointerUp() {
			bg.resetColor();
		}
	}
	
	protected class LabeledTab extends Tab {
		
		private RenderedTextBlock btLabel;
		
		public LabeledTab( String label ) {
			
			super();
			
			btLabel.text( label );
		}
		
		@Override
		protected void createChildren() {
			super.createChildren();
			
			btLabel = PixelScene.renderTextBlock( 9 );
			add( btLabel );
		}
		
		@Override
		protected void layout() {
			super.layout();
			
			btLabel.setPos(
					x + (width - btLabel.width()) / 2,
					y + (height - btLabel.height()) / 2
			);
			PixelScene.align(btLabel);
		}
		
		@Override
		protected void select( boolean value ) {
			super.select( value );
			btLabel.alpha( selected ? 1.0f : 0.6f );
		}
	}
	
	protected class IconTab extends Tab {
		
		protected Image icon;
		private RectF defaultFrame;
		
		public IconTab( Image icon ){
			super();
			
			this.icon.copy(icon);
			this.defaultFrame = icon.frame();
		}
		
		@Override
		protected void createChildren() {
			super.createChildren();
			
			icon = new Image();
			add( icon );
		}
		
		@Override
		protected void layout() {
			super.layout();
			
			icon.frame(defaultFrame);
			icon.x = x + (width - icon.width) / 2;
			icon.y = y + (height - icon.height) / 2;
			/*
			if (!selected) {
				icon.y -= 2;
				//if some of the icon is going into the window, cut it off
				if (icon.y < y + CUT) {
					RectF frame = icon.frame();
					frame.top += (y + CUT - icon.y) / icon.texture.height;
					icon.frame( frame );
					icon.y = y + CUT;
				}
			}

			 */
			PixelScene.align(icon);
		}
		
		@Override
		protected void select( boolean value ) {
			super.select( value );
			icon.am = selected ? 1.0f : 0.6f;
		}
	}

}
