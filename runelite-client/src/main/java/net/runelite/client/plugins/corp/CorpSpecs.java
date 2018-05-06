package net.runelite.client.plugins.corp;

import lombok.Getter;

public class CorpSpecs
{
    @Getter private int dragonWarhammer;
    @Getter private int arclight;
    @Getter private int bandosGodsword;

    public CorpSpecs()
    {
        setInitial();
    }

    void incrementDragonWarhammer()
    {
        dragonWarhammer += 1;
    }

    void incrementArclight()
    {
        arclight += 1;
    }

    void incrementBandosGodsword(int damage)
    {
        bandosGodsword += damage;
    }

    void setInitial() {
        dragonWarhammer = 0;
        arclight = 0;
        bandosGodsword = 0;
    }
}
