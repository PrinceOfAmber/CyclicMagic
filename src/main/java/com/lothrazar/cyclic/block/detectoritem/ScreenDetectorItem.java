package com.lothrazar.cyclic.block.detectoritem;

import com.lothrazar.cyclic.base.ScreenBase;
import com.lothrazar.cyclic.gui.ButtonMachine;
import com.lothrazar.cyclic.gui.TextboxInteger;
import com.lothrazar.cyclic.net.PacketTileData;
import com.lothrazar.cyclic.registry.PacketRegistry;
import com.lothrazar.cyclic.registry.TextureRegistry;
import com.lothrazar.cyclic.util.UtilChat;
import com.mojang.blaze3d.matrix.MatrixStack;
import net.minecraft.entity.player.PlayerInventory;
import net.minecraft.util.text.ITextComponent;

public class ScreenDetectorItem extends ScreenBase<ContainerDetectorItem> {

  private ButtonMachine btnComp;
  private TextboxInteger txtX;
  private TextboxInteger txtY;
  private TextboxInteger txtZ;
  private TextboxInteger txtLimit;

  public ScreenDetectorItem(ContainerDetectorItem screenContainer, PlayerInventory inv, ITextComponent titleIn) {
    super(screenContainer, inv, titleIn);
  }

  @Override
  public void init() {
    super.init();
    int x, y;
    x = guiLeft + 96;
    y = guiTop + 18;
    //    btnEntity = addButton(new ButtonMachine(x, y, 50, 20, "", (p) -> {
    //      int f = TileDetectorItem.Fields.ENTITYTYPE.ordinal();
    //      PacketRegistry.INSTANCE.sendToServer(new PacketTileData(f,
    //          container.tile.getField(f) + 1, container.tile.getPos()));
    //    }));
    y += 28;
    btnComp = addButton(new ButtonMachine(x, y, 50, 20, "", (p) -> {
      int f = TileDetectorItem.Fields.GREATERTHAN.ordinal();
      PacketRegistry.INSTANCE.sendToServer(new PacketTileData(f,
          container.tile.getField(f) + 1, container.tile.getPos()));
    }));
    //x 
    x = guiLeft + 8;
    y = guiTop + 18;
    txtX = new TextboxInteger(this.font, x, y, 20,
        container.tile.getPos(), TileDetectorItem.Fields.RANGEX.ordinal());
    txtX.setText("" + container.tile.getField(TileDetectorItem.Fields.RANGEX.ordinal()));
    txtX.setTooltip(UtilChat.lang("cyclic.detector.rangex"));
    this.children.add(txtX);
    //y 
    x += 30;
    txtY = new TextboxInteger(this.font, x, y, 20,
        container.tile.getPos(), TileDetectorItem.Fields.RANGEY.ordinal());
    txtY.setText("" + container.tile.getField(TileDetectorItem.Fields.RANGEY.ordinal()));
    txtY.setTooltip(UtilChat.lang("cyclic.detector.rangey"));
    this.children.add(txtY);
    x += 30;
    txtZ = new TextboxInteger(this.font, x, y, 20,
        container.tile.getPos(), TileDetectorItem.Fields.RANGEZ.ordinal());
    txtZ.setText("" + container.tile.getField(TileDetectorItem.Fields.RANGEZ.ordinal()));
    txtZ.setTooltip(UtilChat.lang("cyclic.detector.rangez"));
    this.children.add(txtZ);
    x = guiLeft + 38;
    y += 28;
    txtLimit = new TextboxInteger(this.font, x, y, 20,
        container.tile.getPos(), TileDetectorItem.Fields.LIMIT.ordinal());
    txtLimit.setText("" + container.tile.getField(TileDetectorItem.Fields.LIMIT.ordinal()));
    txtLimit.setTooltip(UtilChat.lang("cyclic.detector.limit"));
    this.children.add(txtLimit);
  }
  //  @Override
  //  public void removed() {
  //    this.txtX = null;
  //    this.txtY = null;
  //    this.txtZ = null;
  //    this.txtLimit = null;
  //  }

  @Override
  public void render(MatrixStack ms, int mouseX, int mouseY, float partialTicks) {
    this.renderBackground(ms);
    super.render(ms, mouseX, mouseY, partialTicks);
    this.func_230459_a_(ms, mouseX, mouseY);
  }

  @Override
  protected void drawGuiContainerForegroundLayer(MatrixStack ms, int mouseX, int mouseY) {
    this.drawButtonTooltips(ms, mouseX, mouseY);
    this.drawName(ms, this.title.getString());
    //    btnEntity.setTooltip(UtilChat.lang("cyclic.detector.entitytype.tooltip"));
    //    btnEntity.setMessage(UtilChat.lang("cyclic.detector.entitytype" +
    //        container.tile.getField(TileDetector.Fields.ENTITYTYPE.ordinal())));
    btnComp.setTooltip(UtilChat.lang("cyclic.detector.compare.tooltip"));
    btnComp.setMessage(UtilChat.ilang("cyclic.detector.compare" +
        container.tile.getField(TileDetectorItem.Fields.GREATERTHAN.ordinal())));
  }

  @Override
  protected void drawGuiContainerBackgroundLayer(MatrixStack ms, float partialTicks, int mouseX, int mouseY) {
    this.drawBackground(ms, TextureRegistry.INVENTORY);
    this.txtX.render(ms, mouseX, mouseX, partialTicks);
    this.txtY.render(ms, mouseX, mouseX, partialTicks);
    this.txtZ.render(ms, mouseX, mouseX, partialTicks);
    this.txtLimit.render(ms, mouseX, mouseX, partialTicks);
  }
}