package nars.lab.nario;

import java.awt.Color;
import java.awt.Graphics;
import nars.lab.nario.sprites.Mario;

public class WinScene extends Scene
{
    private MarioComponent component;
    private int tick;
    private String scrollMessage = "Thank you for saving me, Mario!";
    
    public WinScene(MarioComponent component)
    {
        this.component = component;
    }

    public void init()
    {
    }

    public void render(Graphics g, float alpha)
    {
        g.setColor(Color.decode("#8080a0"));
        g.fillRect(0, 0, 320, 240);
        g.drawImage(Art.endScene[tick/24%2][0], 160-48, 100-48, null);
        drawString(g, scrollMessage, 160-scrollMessage.length()*4, 160, 0);
    }

    private void drawString(Graphics g, String text, int x, int y, int c)
    {
        char[] ch = text.toCharArray();
        for (int i = 0; i < ch.length; i++)
        {
            g.drawImage(Art.font[ch[i] - 32][c], x + i * 8, y, null);
        }
    }

    private boolean wasDown = true;
    public void tick()
    {
        tick++;
        if (!wasDown && keys[Mario.KEY_JUMP])
        {
            component.toTitle();
        }
        if (keys[Mario.KEY_JUMP])
        {
            wasDown = false;
        }
    }

    public float getX(float alpha)
    {
        return 0;
    }

    public float getY(float alpha)
    {
        return 0;
    }
}
