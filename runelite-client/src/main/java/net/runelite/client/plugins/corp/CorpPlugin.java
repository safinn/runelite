package net.runelite.client.plugins.corp;

import com.google.common.eventbus.Subscribe;
import net.runelite.api.*;
import net.runelite.api.coords.WorldPoint;
import net.runelite.api.events.ActorDeath;
import net.runelite.api.events.GameTick;
import net.runelite.api.events.HitsplatApplied;
import net.runelite.client.game.ItemManager;
import net.runelite.client.plugins.Plugin;
import net.runelite.client.plugins.PluginDescriptor;
import net.runelite.client.plugins.bosstimer.Boss;
import net.runelite.client.ui.overlay.infobox.Counter;
import net.runelite.client.ui.overlay.infobox.InfoBoxManager;
import net.runelite.client.util.QueryRunner;

import javax.inject.Inject;

@PluginDescriptor(name = "Corporeal Beast")
public class CorpPlugin extends Plugin
{
    @Inject
    private Client client;

    @Inject
    private QueryRunner queryRunner;

    @Inject
    private InfoBoxManager infoBoxManager;

    @Inject
    private ItemManager itemManager;

    private CorpSpecs corpSpecs;

    private Counter dwhCounter;
    private Counter arclightCounter;
    private Counter bgsCounter;

    @Override
    protected void startUp() throws Exception
    {
        corpSpecs = new CorpSpecs();
        dwhCounter =new Counter(itemManager.getImage(ItemID.DRAGON_WARHAMMER), this, Integer.toString(corpSpecs.getDragonWarhammer()));
        arclightCounter = new Counter(itemManager.getImage(ItemID.ARCLIGHT), this, Integer.toString(corpSpecs.getArclight()));
        bgsCounter = new Counter(itemManager.getImage(ItemID.BANDOS_GODSWORD), this, Integer.toString(corpSpecs.getBandosGodsword()));
    }

    @Override
    protected void shutDown() throws Exception
    {
        removeInfoBoxs();
    }

    @Subscribe
    public void onHitsplat(HitsplatApplied hitsplatApplied)
    {
        String name = hitsplatApplied.getActor().getName();
        Actor opponent = hitsplatApplied.getActor().getInteracting();

        if (hitsplatApplied.getHitsplat().getHitsplatType() == Hitsplat.HitsplatType.DAMAGE
                && name.equals(Boss.CORPOREAL_BEAST.getName())
                && opponent.getName().equals(client.getLocalPlayer().getName()))
        {
            int hitAmount = hitsplatApplied.getHitsplat().getAmount();

            switch (opponent.getAnimation())
            {
                case AnimationID.DRAGON_WARHAMMER_SPECIAL_ATTACK:
                    corpSpecs.incrementDragonWarhammer();
                    break;
                case AnimationID.ARCLIGHT_SPECIAL_ATTACK:
                    corpSpecs.incrementArclight();
                    break;
                case AnimationID.BANDOS_GODSWORD_SPECIAL_ATTACK:
                    corpSpecs.incrementBandosGodsword(hitAmount);
                    break;

            }
        }
    }

    @Subscribe
    public void onDeath(ActorDeath actorDeath)
    {
        if (actorDeath.getActor().getName().equals("Corporeal Beast"))
        {
            corpSpecs.setInitial();
        }
    }

    @Subscribe
    public void onGameTick(GameTick gameTick)
    {
        if (atCorpCave() && !infoBoxManager.getInfoBoxes().contains(dwhCounter))
        {
            infoBoxManager.addInfoBox(dwhCounter);
            infoBoxManager.addInfoBox(arclightCounter);
            infoBoxManager.addInfoBox(bgsCounter);
        }
        else if (!atCorpCave())
        {
            removeInfoBoxs();
        }

        if (atCorpCave())
        {
            dwhCounter.setText(Integer.toString(corpSpecs.getDragonWarhammer()));
            arclightCounter.setText(Integer.toString(corpSpecs.getArclight()));
            bgsCounter.setText(Integer.toString(corpSpecs.getBandosGodsword()));
        }
    }

    boolean atCorpCave()
    {
        WorldPoint localWorld = client.getLocalPlayer().getWorldLocation();
        int regionID = localWorld.getRegionID();
        return regionID == 11844;
    }

    private void removeInfoBoxs()
    {
        infoBoxManager.removeInfoBox(dwhCounter);
        infoBoxManager.removeInfoBox(arclightCounter);
        infoBoxManager.removeInfoBox(bgsCounter);
    }
}
