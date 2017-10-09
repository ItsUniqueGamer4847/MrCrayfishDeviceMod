package com.mrcrayfish.device.proxy;

import com.mrcrayfish.device.MrCrayfishDeviceMod;
import com.mrcrayfish.device.Reference;
import com.mrcrayfish.device.api.ApplicationManager;
import com.mrcrayfish.device.core.Laptop;
import com.mrcrayfish.device.init.DeviceBlocks;
import com.mrcrayfish.device.init.DeviceItems;
import com.mrcrayfish.device.object.AppInfo;
import com.mrcrayfish.device.tileentity.TileEntityLaptop;
import com.mrcrayfish.device.tileentity.render.LaptopRenderer;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.texture.DynamicTexture;
import net.minecraft.client.renderer.texture.TextureUtil;
import net.minecraft.util.ResourceLocation;
import net.minecraftforge.fml.client.registry.ClientRegistry;
import net.minecraftforge.fml.relauncher.ReflectionHelper;

import java.awt.*;
import java.awt.image.BufferedImage;
import java.io.IOException;
import java.io.InputStream;

public class ClientProxy implements IProxyInterface
{
    @Override
    public void preInit()
    {
        DeviceBlocks.registerRenders();
        DeviceItems.registerRenders();
    }

    @Override
    public void init()
    {


        ClientRegistry.bindTileEntitySpecialRenderer(TileEntityLaptop.class, new LaptopRenderer());
    }

    @Override
    public void postInit()
    {
        generateIconAtlas();
    }

    public void generateIconAtlas()
    {
        final int ICON_SIZE = 14;
        int index = 0;

        BufferedImage atlas = new BufferedImage(ICON_SIZE * 16, ICON_SIZE * 16, BufferedImage.TYPE_INT_ARGB);
        Graphics g = atlas.createGraphics();

        try
        {
            BufferedImage icon = TextureUtil.readBufferedImage(ClientProxy.class.getResourceAsStream("/assets/" + Reference.MOD_ID + "/textures/icon/missing.png"));
            g.drawImage(icon, 0, 0, ICON_SIZE, ICON_SIZE, null);
        }
        catch(IOException e)
        {
            e.printStackTrace();
        }

        index++;

        for(AppInfo info : ApplicationManager.getAvailableApps())
        {
            ResourceLocation identifier = info.getId();
            String path = "/assets/" + identifier.getResourceDomain() + "/textures/icon/" + identifier.getResourcePath() + ".png";
            try
            {
                InputStream input = ClientProxy.class.getResourceAsStream(path);
                if(input != null)
                {
                    BufferedImage icon = TextureUtil.readBufferedImage(input);
                    if(icon.getWidth() != ICON_SIZE || icon.getHeight() != ICON_SIZE)
                    {
                        MrCrayfishDeviceMod.getLogger().error("Incorrect icon size for " + identifier.toString() + " (Must be 14 by 14 pixels)");
                        continue;
                    }
                    int iconU = (index % 16) * ICON_SIZE;
                    int iconV = (index / 16) * ICON_SIZE;
                    g.drawImage(icon, iconU, iconV, ICON_SIZE, ICON_SIZE, null);
                    updateIcon(info, iconU, iconV);
                    index++;
                }
                else
                {
                    MrCrayfishDeviceMod.getLogger().error("Missing icon for " + identifier.toString());
                }
            }
            catch(Exception e)
            {
                MrCrayfishDeviceMod.getLogger().error("Unable to load icon for " + identifier.toString());
            }
        }

        g.dispose();
        Minecraft.getMinecraft().getTextureManager().loadTexture(Laptop.ICON_TEXTURES, new DynamicTexture(atlas));
    }

    private void updateIcon(AppInfo info, int iconU, int iconV)
    {
        ReflectionHelper.setPrivateValue(AppInfo.class, info, iconU, "iconU");
        ReflectionHelper.setPrivateValue(AppInfo.class, info, iconV, "iconV");
    }
}
