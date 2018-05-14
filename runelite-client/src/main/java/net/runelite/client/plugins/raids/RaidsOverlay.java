/*
 * Copyright (c) 2018, Kamiel
 * All rights reserved.
 *
 * Redistribution and use in source and binary forms, with or without
 * modification, are permitted provided that the following conditions are met:
 *
 * 1. Redistributions of source code must retain the above copyright notice, this
 *    list of conditions and the following disclaimer.
 * 2. Redistributions in binary form must reproduce the above copyright notice,
 *    this list of conditions and the following disclaimer in the documentation
 *    and/or other materials provided with the distribution.
 *
 * THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS "AS IS" AND
 * ANY EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE IMPLIED
 * WARRANTIES OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE ARE
 * DISCLAIMED. IN NO EVENT SHALL THE COPYRIGHT OWNER OR CONTRIBUTORS BE LIABLE FOR
 * ANY DIRECT, INDIRECT, INCIDENTAL, SPECIAL, EXEMPLARY, OR CONSEQUENTIAL DAMAGES
 * (INCLUDING, BUT NOT LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS OR SERVICES;
 * LOSS OF USE, DATA, OR PROFITS; OR BUSINESS INTERRUPTION) HOWEVER CAUSED AND
 * ON ANY THEORY OF LIABILITY, WHETHER IN CONTRACT, STRICT LIABILITY, OR TORT
 * (INCLUDING NEGLIGENCE OR OTHERWISE) ARISING IN ANY WAY OUT OF THE USE OF THIS
 * SOFTWARE, EVEN IF ADVISED OF THE POSSIBILITY OF SUCH DAMAGE.
 */
package net.runelite.client.plugins.raids;

import java.awt.Color;
import java.awt.Dimension;
import java.awt.Graphics2D;
import java.awt.Polygon;
import java.util.List;
import javax.inject.Inject;
import lombok.Setter;
import net.runelite.api.Client;
import net.runelite.api.Perspective;
import net.runelite.api.coords.LocalPoint;
import net.runelite.api.events.ProjectileMoved;
import net.runelite.client.plugins.raids.solver.Room;
import net.runelite.client.ui.overlay.Overlay;
import net.runelite.client.ui.overlay.OverlayPosition;
import net.runelite.client.ui.overlay.OverlayPriority;
import net.runelite.client.ui.overlay.OverlayUtil;
import net.runelite.client.ui.overlay.components.LineComponent;
import net.runelite.client.ui.overlay.components.PanelComponent;
import net.runelite.client.ui.overlay.components.TitleComponent;

public class RaidsOverlay extends Overlay
{
	private RaidsPlugin plugin;
	private RaidsConfig config;
	private Client client;
	private final PanelComponent panelComponent = new PanelComponent();

	@Setter
	private boolean scoutOverlayShown = false;

	@Inject
	public RaidsOverlay(RaidsPlugin plugin, RaidsConfig config, Client client)
	{
		setPosition(OverlayPosition.TOP_LEFT);
		setPriority(OverlayPriority.LOW);
		this.plugin = plugin;
		this.config = config;
		this.client = client;
	}

	@Override
	public Dimension render(Graphics2D graphics)
	{
		renderProjectiles(graphics);

		if (!config.scoutOverlay() || !scoutOverlayShown)
		{
			return null;
		}

		panelComponent.getChildren().clear();

		if (plugin.getRaid() == null || plugin.getRaid().getLayout() == null)
		{
			panelComponent.getChildren().add(TitleComponent.builder()
				.text("Unable to scout this raid!")
				.color(Color.RED)
				.build());

			return panelComponent.render(graphics);
		}

		panelComponent.getChildren().add(TitleComponent.builder()
			.text("Raid scouter")
			.build());

		Color color = Color.WHITE;
		String layout = plugin.getRaid().getLayout().toCode().replaceAll("#", "").replaceAll("¤", "");

		if (config.enableLayoutWhitelist() && !plugin.getLayoutWhitelist().contains(layout.toLowerCase()))
		{
			color = Color.RED;
		}

		panelComponent.getChildren().add(LineComponent.builder()
			.left("Layout")
			.right(layout)
			.rightColor(color)
			.build());

		int bossMatches = 0;
		int bossCount = 0;

		if (config.enableRotationWhitelist())
		{
			bossMatches = plugin.getRotationMatches();
		}

		for (Room layoutRoom : plugin.getRaid().getLayout().getRooms())
		{
			int position = layoutRoom.getPosition();
			RaidRoom room = plugin.getRaid().getRoom(position);

			if (room == null)
			{
				continue;
			}

			color = Color.WHITE;

			switch (room.getType())
			{
				case COMBAT:
					bossCount++;
					if (plugin.getRoomWhitelist().contains(room.getBoss().getName().toLowerCase()))
					{
						color = Color.GREEN;
					}
					else if (plugin.getRoomBlacklist().contains(room.getBoss().getName().toLowerCase())
							|| config.enableRotationWhitelist() && bossCount > bossMatches)
					{
						color = Color.RED;
					}

					panelComponent.getChildren().add(LineComponent.builder()
						.left(room.getType().getName())
						.right(room.getBoss().getName())
						.rightColor(color)
						.build());

					break;

				case PUZZLE:
					if (plugin.getRoomWhitelist().contains(room.getPuzzle().getName().toLowerCase()))
					{
						color = Color.GREEN;
					}
					else if (plugin.getRoomBlacklist().contains(room.getPuzzle().getName().toLowerCase()))
					{
						color = Color.RED;
					}

					panelComponent.getChildren().add(LineComponent.builder()
						.left(room.getType().getName())
						.right(room.getPuzzle().getName())
						.rightColor(color)
						.build());
					break;
			}
		}

		return panelComponent.render(graphics);
	}

	private void renderProjectiles(Graphics2D graphics)
	{
		List<ProjectileMoved> projectileFiredlist = plugin.getProjectileMovedlist();
		for (ProjectileMoved projectileMoved : projectileFiredlist)
		{
			LocalPoint localPoint = projectileMoved.getPosition();
			Polygon poly = Perspective.getCanvasTilePoly(client, localPoint);

			if (poly != null)
			{
				OverlayUtil.renderPolygon(graphics, poly, Color.RED);
			}
		}
	}
}
