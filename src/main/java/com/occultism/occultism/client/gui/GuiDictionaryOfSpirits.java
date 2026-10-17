package com.occultism.occultism.client.gui;

import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;

import com.occultism.occultism.Occultism;
import com.occultism.occultism.Tags;
import com.occultism.occultism.registry.ModBlocks;
import com.occultism.occultism.registry.ModItems;
import com.occultism.occultism.ritual.PentacleDefinition;
import com.occultism.occultism.ritual.PentacleDetector;
import com.occultism.occultism.ritual.RitualDefinition;
import com.occultism.occultism.ritual.RitualIngredient;
import com.occultism.occultism.ritual.RitualRegistry;
import com.occultism.occultism.ritual.SpiritFireRecipeRegistry;
import com.occultism.occultism.spirit.SpiritJobType;
import com.occultism.occultism.spirit.SpiritTradeRecipeRegistry;
import com.occultism.occultism.spirit.SpiritTradeIngredient;

import net.minecraft.client.gui.GuiButton;
import net.minecraft.client.gui.GuiScreen;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.Gui;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.client.renderer.RenderHelper;
import net.minecraft.client.resources.I18n;
import net.minecraft.entity.EntityList;
import net.minecraft.init.Blocks;
import net.minecraft.init.Items;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.item.crafting.FurnaceRecipes;
import net.minecraft.util.ResourceLocation;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;
import net.minecraftforge.fml.common.registry.ForgeRegistries;
import net.minecraftforge.oredict.OreDictionary;
import org.lwjgl.input.Keyboard;
import org.lwjgl.input.Mouse;

@SideOnly(Side.CLIENT)
public class GuiDictionaryOfSpirits extends GuiScreen {

    private static final int PANEL_WIDTH = 360;
    private static final int PANEL_HEIGHT = 226;
    private static final int BOOK_WIDTH = 272;
    private static final int BOOK_HEIGHT = 178;
    private static final int BOOK_TEXTURE_WIDTH = 272;
    private static final int BOOK_TEXTURE_HEIGHT = 178;
    private static final int BOOK_PAGE_TOP = 27;
    private static final int BOOK_PAGE_BOTTOM = 153;
    private static final int BOOK_LEFT_PAGE_X = 22;
    private static final int BOOK_RIGHT_PAGE_X = 150;
    private static final int BOOK_PAGE_WIDTH = 102;
    private static final int BODY_WIDTH = BOOK_PAGE_WIDTH;
    private static final int BODY_TOP = BOOK_PAGE_TOP;
    private static final int BODY_BOTTOM_MARGIN = PANEL_HEIGHT - BOOK_PAGE_BOTTOM;
    private static final int CATEGORY_BUTTON_WIDTH = 32;
    private static final int CATEGORY_BUTTON_HEIGHT = 18;
    private static final int CATEGORY_BUTTON_SPACING = 21;
    private static final int NODE_SIZE = 26;
    private static final int NODE_ICON_SIZE = 16;
    private static final int NODE_SPACING = 38;
    private static final int NODE_SCROLL_STEP = 48;
    private static final int GRAPH_LEFT_MARGIN = 48;
    private static final int GRAPH_TOP = 22;
    private static final int GRAPH_WIDTH = 300;
    private static final int GRAPH_HEIGHT = 172;
    private static final int NAV_Y_OFFSET = 196;
    private static final int IMAGE_SPACING = 6;
    private static final int SPOTLIGHT_BOX_SIZE = 34;
    private static final int SPOTLIGHT_BODY_SPACING = 8;
    private static final int REFERENCE_HEADER_HEIGHT = 58;
    private static final int REFERENCE_BODY_SPACING = 6;
    private static final int REFERENCE_SLOT_SIZE = 18;
    private static final int REFERENCE_SLOT_SPACING = 4;
    private static final int CRAFTING_SLOT_SPACING = 2;
    private static final int CRAFTING_GRID_SIZE = 3;
    private static final int CRAFTING_ARROW_WIDTH = 14;
    private static final int CRAFTING_PREVIEW_SPACING = 6;
    private static final int PENTACLE_PREVIEW_MAX_HEIGHT = 82;
    private static final int PENTACLE_PREVIEW_SPACING = 6;
    private static final int RITUAL_DUMMY_OFFSET = 10;
    private static final int ID_PREVIOUS = 1;
    private static final int ID_NEXT = 2;
    private static final int ID_CONTENTS = 3;
    private static final int ID_CLOSE = 4;
    private static final int ID_VISUALIZE = 5;
    private static final int ID_CATEGORY_BASE = 100;
    private static final int ID_ENTRY_BASE = 200;
    private static final int NO_CATEGORY = -1;
    private static final int NO_ENTRY = -1;
    private static final int PANEL = 0xFF2D2424;
    private static final int HEADER = 0xFF3B2826;
    private static final int SLOT = 0xFFEFE6C8;
    private static final int SLOT_EDGE = 0xFF7B6B54;
    private static final int TEXT = 0xFF1E1713;
    private static final int MUTED = 0xFF5D5143;
    private static final int ACCENT = 0xFF7A1CA0;
    private static final int NODE_LINE = 0xFFD4D6D8;
    private static final int NODE_LINE_SHADOW = 0xFF6E7072;

    private static final ResourceLocation DARK_SLATE_BACKGROUND = new ResourceLocation("modonomicon",
            "textures/gui/dark_slate_seamless.png");
    private static final ResourceLocation DOUBLE_PAGE_BACKGROUND = new ResourceLocation("modonomicon",
            "textures/gui/sprites/modonomicon/themes/default/content/backgrounds/book/double_page_background.png");
    private static final ResourceLocation CATEGORY_BUTTON_NORMAL = new ResourceLocation("modonomicon",
            "textures/gui/sprites/modonomicon/themes/default/content/buttons/category/category_button_normal.png");
    private static final ResourceLocation CATEGORY_BUTTON_GOLDEN = new ResourceLocation("modonomicon",
            "textures/gui/sprites/modonomicon/themes/default/content/buttons/category/category_button_golden.png");
    private static final ResourceLocation PREVIOUS_BUTTON = new ResourceLocation("modonomicon",
            "textures/gui/sprites/modonomicon/themes/default/content/buttons/navigation/previous_page_button_normal.png");
    private static final ResourceLocation NEXT_BUTTON = new ResourceLocation("modonomicon",
            "textures/gui/sprites/modonomicon/themes/default/content/buttons/navigation/next_page_button_normal.png");
    private static final ResourceLocation BACK_BUTTON = new ResourceLocation("modonomicon",
            "textures/gui/sprites/modonomicon/themes/default/content/buttons/navigation/back_button_normal.png");
    private static final ResourceLocation EXIT_BUTTON = new ResourceLocation("modonomicon",
            "textures/gui/sprites/modonomicon/themes/default/content/buttons/navigation/exit_button_normal.png");
    private static final ResourceLocation VISUALIZE_BUTTON = new ResourceLocation("modonomicon",
            "textures/gui/sprites/modonomicon/themes/default/content/buttons/navigation/visualize_button_normal.png");

    private static final String UPSTREAM_INDEX = "assets/" + Tags.MOD_ID
            + "/dictionary/modonomicon_index.json";

    private static final GuideCategory[] CATEGORIES = loadCategories();

    private static GuideCategory[] fallbackCategories() {
        return new GuideCategory[] {
            category("getting_started",
                    entry("about"),
                    entry("getting_started_route"),
                    entry("spirit_fire"),
                    entry("spirit_fire_inputs"),
                    entry("datura_third_eye"),
                    entry("third_eye_potions"),
                    entry("divination"),
                    entry("divination_use"),
                    entry("chalks"),
                    entry("chalk_use"),
                    entry("first_ritual"),
                    entry("ritual_bowls")),
            category("spirits",
                    entry("books_of_binding"),
                    entry("binding_materials"),
                    entry("summoning"),
                    entry("high_tier_spirits"),
                    entry("spirit_names_tiers"),
                    entry("unbound_afrit"),
                    entry("soul_shards")),
            category("rituals",
                    entry("rituals"),
                    entry("ritual_steps"),
                    entry("crafting_rituals"),
                    entry("item_use_rituals"),
                    entry("sacrifice_rituals"),
                    entry("repair_rituals"),
                    entry("item_use_flow")),
            category("pentacles",
                    entry("pentacles"),
                    entry("pentacle_learning"),
                    entry("pentacle_chalk_tiers"),
                    entry("pentacle_apparatus"),
                    entry("pentacle_craft_foliot"),
                    entry("pentacle_craft_djinni"),
                    entry("pentacle_craft_afrit"),
                    entry("pentacle_craft_marid")),
            category("summoning_rituals",
                    entry("weather_time_rituals"),
                    entry("weather_time_details"),
                    entry("wondering_trader"),
                    entry("calling_books"),
                    entry("calling_book_configuration"),
                    entry("foliot_jobs"),
                    entry("foliot_job_setup"),
                    entry("djinni_jobs"),
                    entry("afrit_jobs"),
                    entry("marid_jobs"),
                    entry("spirit_processing")),
            category("possession_rituals",
                    entry("possession"),
                    entry("possession_targets"),
                    entry("possessed_phantom"),
                    entry("otherworld_bird"),
                    entry("possessed_hoglin"),
                    entry("possessed_warden"),
                    entry("mercy_goat"),
                    entry("possessed_bee"),
                    entry("wild_spirit_hordes"),
                    entry("possession_substitutes"),
                    entry("random_animals")),
            category("familiar_rituals",
                    entry("familiar_parrot"),
                    entry("familiar_greedy"),
                    entry("familiar_cthulhu"),
                    entry("familiar_blacksmith"),
                    entry("familiar_dragon"),
                    entry("familiar_fairy"),
                    entry("familiar_shub_niggurath"),
                    entry("familiar_beholder"),
                    entry("familiar_chimera"),
                    entry("iesnium_golem"),
                    entry("animal_armor"),
                    entry("resurrection"),
                    entry("resurrect_allay")),
            category("crafting_rituals",
                    entry("otherworld"),
                    entry("otherworld_blocks"),
                    entry("otherworld_trees"),
                    entry("otherworld_tools"),
                    entry("otherworld_goggles"),
                    entry("infused_pickaxe"),
                    entry("satchels"),
                    entry("ritual_satchels"),
                    entry("ritual_satchel_use"),
                    entry("soul_gems"),
                    entry("soul_gem_capture"),
                    entry("spirit_grindstone"),
                    entry("nature_gray_paste"),
                    entry("butcher_knife_tallow"),
                    entry("materials_metals"),
                    entry("silver"),
                    entry("iesnium_ore"),
                    entry("magic_tools"),
                    entry("knowledge_vitality"),
                    entry("magic_lamps_detail"),
                    entry("iesnium_anvil"),
                    entry("tools_items")),
            category("storage",
                    entry("mining_storage"),
                    entry("dimensional_mineshaft"),
                    entry("dimensional_battlefield"),
                    entry("miner_spirits"),
                    entry("dimensional_extractor"),
                    entry("storage_remote"),
                    entry("storage_actuators"),
                    entry("wormholes"),
                    entry("wormhole_blocks"))
        };
    }

    private int categoryIndex = getDefaultCategoryIndex();
    private int entryIndex = NO_ENTRY;
    private int pageIndex = 0;
    private int nodeScrollY = 0;
    private int bodyScroll = 0;
    private int currentMouseX = 0;
    private int currentMouseY = 0;
    private ItemStack hoveredStack = ItemStack.EMPTY;

    @Override
    public void initGui() {
        Keyboard.enableRepeatEvents(true);
        this.buttonList.clear();
        int left = (this.width - PANEL_WIDTH) / 2;
        int top = (this.height - PANEL_HEIGHT) / 2;
        int bookLeft = left + (PANEL_WIDTH - BOOK_WIDTH) / 2;
        int bookTop = top + 8;
        int navY = top + NAV_Y_OFFSET;

        if (this.isEntryPage()) {
            this.clampBodyScroll();
            this.buttonList.add(new IconButton(ID_PREVIOUS, bookLeft + 53, navY, 18, 10,
                    PREVIOUS_BUTTON, 18, 10, I18n.format("gui.occultism.dictionary.previous")));
            this.buttonList.add(new IconButton(ID_NEXT, bookLeft + 200, navY, 18, 10,
                    NEXT_BUTTON, 18, 10, I18n.format("gui.occultism.dictionary.next")));
            GuidePage page = this.getCurrentEntries()[this.entryIndex].pages[this.pageIndex];
            if (page.hasReference() && page.reference.hasPentaclePreview()) {
                this.buttonList.add(new IconButton(ID_VISUALIZE, bookLeft + 246, navY - 1, 11, 7,
                        VISUALIZE_BUTTON, 11, 7, I18n.format("gui.occultism.dictionary.visualize")));
            }
        } else {
            this.nodeScrollY = Math.max(0, Math.min(this.nodeScrollY, this.getMaxListRowOffset()));
            this.addListButtons(left, top);
            this.buttonList.add(new IconButton(ID_PREVIOUS, left + GRAPH_LEFT_MARGIN, navY, 18, 10,
                    PREVIOUS_BUTTON, 18, 10, I18n.format("gui.occultism.dictionary.previous")));
            this.buttonList.add(new IconButton(ID_NEXT, left + GRAPH_LEFT_MARGIN + GRAPH_WIDTH - 18, navY, 18, 10,
                    NEXT_BUTTON, 18, 10, I18n.format("gui.occultism.dictionary.next")));
        }

        String contentsLabel = I18n.format("gui.occultism.dictionary.back");
        this.buttonList.add(new IconButton(ID_CONTENTS, bookLeft + 126, navY, 18, 9,
                BACK_BUTTON, 18, 9, contentsLabel));
        this.buttonList.add(new IconButton(ID_CLOSE, bookLeft + 260, bookTop + 8, 12, 12,
                EXIT_BUTTON, 12, 12, I18n.format("gui.occultism.dictionary.close")));
        this.updateNavigationButtons();
    }

    @Override
    public void onGuiClosed() {
        Keyboard.enableRepeatEvents(false);
    }

    @Override
    protected void actionPerformed(GuiButton button) throws IOException {
        if (button.id == ID_PREVIOUS) {
            this.handlePrevious();
        } else if (button.id == ID_NEXT) {
            this.handleNext();
        } else if (button.id == ID_CONTENTS) {
            this.handleContents();
        } else if (button.id == ID_CLOSE) {
            this.mc.displayGuiScreen(null);
        } else if (button.id == ID_VISUALIZE) {
            this.bodyScroll = Math.max(0, Math.min(this.getMaxBodyScroll(), this.bodyScroll));
        } else if (button.id >= ID_CATEGORY_BASE && button.id < ID_CATEGORY_BASE + CATEGORIES.length) {
            this.categoryIndex = button.id - ID_CATEGORY_BASE;
            this.entryIndex = NO_ENTRY;
            this.nodeScrollY = 0;
            this.bodyScroll = 0;
            this.initGui();
        } else if (button.id >= ID_ENTRY_BASE && this.isCategoryContents()) {
            int selected = button.id - ID_ENTRY_BASE;
            if (selected >= 0 && selected < this.getCurrentEntries().length) {
                this.entryIndex = selected;
                this.pageIndex = 0;
                this.bodyScroll = 0;
                this.initGui();
            }
        }
    }

    @Override
    protected void mouseClicked(int mouseX, int mouseY, int mouseButton) throws IOException {
        if (mouseButton == 0 && this.isEntryPage() && this.handleRitualPentacleClick(mouseX, mouseY)) {
            return;
        }
        super.mouseClicked(mouseX, mouseY, mouseButton);
    }

    @Override
    protected void keyTyped(char typedChar, int keyCode) throws IOException {
        if (keyCode == Keyboard.KEY_LEFT) {
            this.handlePrevious();
            return;
        }
        if (keyCode == Keyboard.KEY_RIGHT) {
            this.handleNext();
            return;
        }
        if (keyCode == Keyboard.KEY_UP && this.isEntryPage()) {
            this.bodyScroll = Math.max(0, this.bodyScroll - 1);
            return;
        }
        if (keyCode == Keyboard.KEY_DOWN && this.isEntryPage()) {
            this.bodyScroll = Math.min(this.getMaxBodyScroll(), this.bodyScroll + 1);
            return;
        }
        if (keyCode == Keyboard.KEY_UP && !this.isEntryPage()) {
            this.nodeScrollY = Math.max(0, this.nodeScrollY - NODE_SCROLL_STEP);
            this.initGui();
            return;
        }
        if (keyCode == Keyboard.KEY_DOWN && !this.isEntryPage()) {
            this.nodeScrollY = Math.min(this.getMaxListRowOffset(), this.nodeScrollY + NODE_SCROLL_STEP);
            this.initGui();
            return;
        }
        super.keyTyped(typedChar, keyCode);
    }

    @Override
    public void handleMouseInput() throws IOException {
        super.handleMouseInput();
        int wheel = Mouse.getEventDWheel();
        if (wheel == 0) {
            return;
        }

        int direction = wheel < 0 ? 1 : -1;
        if (this.isEntryPage()) {
            this.bodyScroll = Math.max(0, Math.min(this.getMaxBodyScroll(), this.bodyScroll + direction * 3));
        } else {
            this.nodeScrollY = Math.max(0,
                    Math.min(this.getMaxListRowOffset(), this.nodeScrollY + direction * NODE_SCROLL_STEP));
            this.initGui();
        }
    }

    @Override
    public void drawScreen(int mouseX, int mouseY, float partialTicks) {
        this.currentMouseX = mouseX;
        this.currentMouseY = mouseY;
        this.hoveredStack = ItemStack.EMPTY;
        this.drawDefaultBackground();
        int left = (this.width - PANEL_WIDTH) / 2;
        int top = (this.height - PANEL_HEIGHT) / 2;
        this.drawPanel(left, top);

        if (this.isEntryPage()) {
            this.drawEntry(left, top);
        } else {
            this.ensureCategorySelected();
            this.drawCategoryContents(left, top);
        }
        super.drawScreen(mouseX, mouseY, partialTicks);
        this.drawHoveredStackTooltip(mouseX, mouseY);
    }

    @Override
    public boolean doesGuiPauseGame() {
        return false;
    }

    private void addListButtons(int left, int top) {
        this.addCategoryTabButtons(left, top);
        GuideEntry[] entries = this.getCurrentEntries();
        for (int index = 0; index < entries.length; index++) {
            int[] point = this.getEntryNodePoint(entries[index], left, top);
            if (!this.isInsideGraph(point[0], point[1], left, top)) {
                continue;
            }
            this.buttonList.add(new InvisibleButton(ID_ENTRY_BASE + index,
                    point[0] - NODE_SIZE / 2, point[1] - NODE_SIZE / 2, NODE_SIZE, NODE_SIZE,
                    I18n.format(entries[index].titleKey)));
        }
    }

    private void addCategoryTabButtons(int left, int top) {
        int startY = top + 34;
        for (int i = 0; i < CATEGORIES.length; i++) {
            int y = startY + i * CATEGORY_BUTTON_SPACING;
            if (y + CATEGORY_BUTTON_HEIGHT < top + 18 || y > top + GRAPH_TOP + GRAPH_HEIGHT) {
                continue;
            }
            this.buttonList.add(new InvisibleButton(ID_CATEGORY_BASE + i,
                    left + 7, y, CATEGORY_BUTTON_WIDTH, CATEGORY_BUTTON_HEIGHT,
                    I18n.format(CATEGORIES[i].titleKey)));
        }
    }

    private void drawPanel(int left, int top) {
        if (this.isEntryPage()) {
            this.drawBookBackground(left, top);
        } else {
            this.drawCategoryBackground(left, top);
        }
    }

    private void drawCategoryContents(int left, int top) {
        GuideCategory category = CATEGORIES[this.categoryIndex];
        this.drawCategoryTabs(left, top);
        this.drawEntryConnections(category, left, top);
        for (GuideEntry entry : category.entries) {
            int[] point = this.getEntryNodePoint(entry, left, top);
            if (!this.isInsideGraph(point[0], point[1], left, top)) {
                continue;
            }
            this.drawNodeBackground(entry.nodeBackground, point[0] - NODE_SIZE / 2, point[1] - NODE_SIZE / 2);
            this.drawIcon(entry.icon, point[0] - NODE_ICON_SIZE / 2, point[1] - NODE_ICON_SIZE / 2, NODE_ICON_SIZE);
        }
        this.drawCenteredString(this.fontRenderer, I18n.format(category.titleKey),
                left + GRAPH_LEFT_MARGIN + GRAPH_WIDTH / 2, top + 10, 0xFFE7E7E7);
    }

    private void drawEntry(int left, int top) {
        GuideEntry entry = this.getCurrentEntries()[this.entryIndex];
        GuidePage page = entry.pages[this.pageIndex];
        int bookLeft = left + (PANEL_WIDTH - BOOK_WIDTH) / 2;
        int bookTop = top + 8;
        int leftPageX = bookLeft + BOOK_LEFT_PAGE_X;
        int rightPageX = bookLeft + BOOK_RIGHT_PAGE_X;
        int pageY = bookTop + BOOK_PAGE_TOP;
        int pageBottom = bookTop + BOOK_PAGE_BOTTOM;

        this.drawCenteredString(this.fontRenderer, I18n.format(entry.titleKey),
                bookLeft + BOOK_WIDTH / 2, bookTop + 24, TEXT);
        if (page.hasImages()) {
            this.drawImagePage(page, leftPageX, pageY, BOOK_PAGE_WIDTH, pageBottom);
        } else if (page.hasSpotlight()) {
            this.drawSpotlightPage(page, leftPageX, pageY, BOOK_PAGE_WIDTH, pageBottom);
        } else if (page.hasReference() && page.reference.hasRitualPreview()) {
            this.drawRitualReference(page.reference, leftPageX, pageY, BOOK_PAGE_WIDTH, pageBottom);
        } else if (page.hasReference()) {
            this.drawReferencePage(page, leftPageX, pageY, BOOK_PAGE_WIDTH, pageBottom);
        } else {
            this.drawWrappedInBounds(this.getCurrentPageBody(page), leftPageX, pageY, BOOK_PAGE_WIDTH,
                    pageBottom, TEXT);
        }
        String category = I18n.format(CATEGORIES[this.categoryIndex].titleKey);
        this.fontRenderer.drawString(category, rightPageX, pageY, MUTED);
        this.drawEntryMetadata(entry, page, rightPageX, pageY + 12, BOOK_PAGE_WIDTH, pageBottom);
        String entryPageLabel = I18n.format("gui.occultism.dictionary.page", this.entryIndex + 1,
                this.getCurrentEntries().length);
        String pageText = entryPageLabel + " / " + (this.pageIndex + 1) + "/" + entry.pages.length;
        this.fontRenderer.drawString(pageText, rightPageX + BOOK_PAGE_WIDTH - this.fontRenderer.getStringWidth(pageText),
                pageBottom - this.fontRenderer.FONT_HEIGHT, MUTED);
        if (this.getMaxBodyScroll() > 0) {
            String hint = I18n.format("gui.occultism.dictionary.scroll_hint");
            this.fontRenderer.drawString(hint, leftPageX, pageBottom - this.fontRenderer.FONT_HEIGHT, MUTED);
        }
    }

    private void drawBookBackground(int left, int top) {
        int bookLeft = left + (PANEL_WIDTH - BOOK_WIDTH) / 2;
        int bookTop = top + 8;
        GlStateManager.color(1.0F, 1.0F, 1.0F, 1.0F);
        this.mc.getTextureManager().bindTexture(DOUBLE_PAGE_BACKGROUND);
        drawModalRectWithCustomSizedTexture(bookLeft, bookTop, 0.0F, 0.0F, BOOK_WIDTH, BOOK_HEIGHT,
                BOOK_TEXTURE_WIDTH, BOOK_TEXTURE_HEIGHT);
    }

    private void drawCategoryBackground(int left, int top) {
        GlStateManager.color(1.0F, 1.0F, 1.0F, 1.0F);
        this.mc.getTextureManager().bindTexture(DARK_SLATE_BACKGROUND);
        int tile = 128;
        for (int x = left + GRAPH_LEFT_MARGIN; x < left + GRAPH_LEFT_MARGIN + GRAPH_WIDTH; x += tile) {
            for (int y = top + GRAPH_TOP; y < top + GRAPH_TOP + GRAPH_HEIGHT; y += tile) {
                int width = Math.min(tile, left + GRAPH_LEFT_MARGIN + GRAPH_WIDTH - x);
                int height = Math.min(tile, top + GRAPH_TOP + GRAPH_HEIGHT - y);
                drawModalRectWithCustomSizedTexture(x, y, 0.0F, 0.0F, width, height, 512.0F, 512.0F);
            }
        }
        drawRect(left + GRAPH_LEFT_MARGIN - 2, top + GRAPH_TOP - 2,
                left + GRAPH_LEFT_MARGIN + GRAPH_WIDTH + 2, top + GRAPH_TOP, HEADER);
        drawRect(left + GRAPH_LEFT_MARGIN - 2, top + GRAPH_TOP + GRAPH_HEIGHT,
                left + GRAPH_LEFT_MARGIN + GRAPH_WIDTH + 2, top + GRAPH_TOP + GRAPH_HEIGHT + 2, HEADER);
        drawRect(left + GRAPH_LEFT_MARGIN - 2, top + GRAPH_TOP - 2,
                left + GRAPH_LEFT_MARGIN, top + GRAPH_TOP + GRAPH_HEIGHT + 2, HEADER);
        drawRect(left + GRAPH_LEFT_MARGIN + GRAPH_WIDTH, top + GRAPH_TOP - 2,
                left + GRAPH_LEFT_MARGIN + GRAPH_WIDTH + 2, top + GRAPH_TOP + GRAPH_HEIGHT + 2, HEADER);
    }

    private void drawCategoryTabs(int left, int top) {
        int startY = top + 34;
        for (int i = 0; i < CATEGORIES.length; i++) {
            int y = startY + i * CATEGORY_BUTTON_SPACING;
            if (y + CATEGORY_BUTTON_HEIGHT < top + 18 || y > top + GRAPH_TOP + GRAPH_HEIGHT) {
                continue;
            }
            ResourceLocation texture = i == this.categoryIndex ? CATEGORY_BUTTON_GOLDEN : CATEGORY_BUTTON_NORMAL;
            GlStateManager.color(1.0F, 1.0F, 1.0F, 1.0F);
            this.mc.getTextureManager().bindTexture(texture);
            drawModalRectWithCustomSizedTexture(left + 7, y, 0.0F, 0.0F,
                    CATEGORY_BUTTON_WIDTH, CATEGORY_BUTTON_HEIGHT, CATEGORY_BUTTON_WIDTH, CATEGORY_BUTTON_HEIGHT);
            this.drawIcon(CATEGORIES[i].icon, left + 15, y + 1, NODE_ICON_SIZE);
        }
    }

    private void drawEntryConnections(GuideCategory category, int left, int top) {
        Map<String, GuideEntry> entriesById = new HashMap<>();
        for (GuideEntry entry : category.entries) {
            entriesById.put(entry.fullId, entry);
        }
        for (GuideEntry entry : category.entries) {
            int[] child = this.getEntryNodePoint(entry, left, top);
            for (String parentId : entry.parents) {
                GuideEntry parent = entriesById.get(parentId);
                if (parent == null) {
                    continue;
                }
                int[] parentPoint = this.getEntryNodePoint(parent, left, top);
                this.drawConnection(parentPoint[0], parentPoint[1], child[0], child[1], left, top);
            }
        }
    }

    private void drawConnection(int x1, int y1, int x2, int y2, int left, int top) {
        if (!this.isInsideGraph(x1, y1, left, top) && !this.isInsideGraph(x2, y2, left, top)) {
            return;
        }
        this.drawHorizontalLineSafe(Math.min(x1, x2), Math.max(x1, x2), y1, NODE_LINE_SHADOW, left, top);
        this.drawVerticalLineSafe(x2, Math.min(y1, y2), Math.max(y1, y2), NODE_LINE_SHADOW, left, top);
        this.drawHorizontalLineSafe(Math.min(x1, x2), Math.max(x1, x2), y1 - 1, NODE_LINE, left, top);
        this.drawVerticalLineSafe(x2 - 1, Math.min(y1, y2), Math.max(y1, y2), NODE_LINE, left, top);
    }

    private void drawHorizontalLineSafe(int startX, int endX, int y, int color, int left, int top) {
        int minX = left + GRAPH_LEFT_MARGIN;
        int maxX = left + GRAPH_LEFT_MARGIN + GRAPH_WIDTH;
        int minY = top + GRAPH_TOP;
        int maxY = top + GRAPH_TOP + GRAPH_HEIGHT;
        if (y < minY || y > maxY) {
            return;
        }
        drawRect(Math.max(startX, minX), y, Math.min(endX, maxX), y + 1, color);
    }

    private void drawVerticalLineSafe(int x, int startY, int endY, int color, int left, int top) {
        int minX = left + GRAPH_LEFT_MARGIN;
        int maxX = left + GRAPH_LEFT_MARGIN + GRAPH_WIDTH;
        int minY = top + GRAPH_TOP;
        int maxY = top + GRAPH_TOP + GRAPH_HEIGHT;
        if (x < minX || x > maxX) {
            return;
        }
        drawRect(x, Math.max(startY, minY), x + 1, Math.min(endY, maxY), color);
    }

    private int[] getEntryNodePoint(GuideEntry entry, int left, int top) {
        GuideCategory category = CATEGORIES[this.categoryIndex];
        int originX = left + GRAPH_LEFT_MARGIN + GRAPH_WIDTH / 2
                - (category.minX + category.maxX) * NODE_SPACING / 2;
        int originY = top + GRAPH_TOP + GRAPH_HEIGHT / 2
                - (category.minY + category.maxY) * NODE_SPACING / 2;
        return new int[] {
            originX + entry.x * NODE_SPACING,
            originY + entry.y * NODE_SPACING - this.nodeScrollY
        };
    }

    private boolean isInsideGraph(int x, int y, int left, int top) {
        return x >= left + GRAPH_LEFT_MARGIN - NODE_SIZE
                && x <= left + GRAPH_LEFT_MARGIN + GRAPH_WIDTH + NODE_SIZE
                && y >= top + GRAPH_TOP - NODE_SIZE
                && y <= top + GRAPH_TOP + GRAPH_HEIGHT + NODE_SIZE;
    }

    private void drawNodeBackground(ResourceLocation background, int x, int y) {
        GlStateManager.color(1.0F, 1.0F, 1.0F, 1.0F);
        this.mc.getTextureManager().bindTexture(background);
        drawModalRectWithCustomSizedTexture(x, y, 0.0F, 0.0F, NODE_SIZE, NODE_SIZE, NODE_SIZE, NODE_SIZE);
    }

    private void drawIcon(GuideIcon icon, int x, int y, int size) {
        if (icon.texture != null) {
            GlStateManager.color(1.0F, 1.0F, 1.0F, 1.0F);
            this.mc.getTextureManager().bindTexture(icon.texture);
            drawModalRectWithCustomSizedTexture(x, y, 0.0F, 0.0F, size, size, icon.width, icon.height);
            return;
        }
        if (!icon.stack.isEmpty()) {
            RenderHelper.enableGUIStandardItemLighting();
            this.mc.getRenderItem().renderItemAndEffectIntoGUI(icon.stack, x, y);
            this.mc.getRenderItem().renderItemOverlays(this.fontRenderer, icon.stack, x, y);
            RenderHelper.disableStandardItemLighting();
            GlStateManager.disableLighting();
            GlStateManager.color(1.0F, 1.0F, 1.0F, 1.0F);
        }
    }

    private void drawEntryMetadata(GuideEntry entry, GuidePage page, int x, int y, int width, int bottom) {
        this.drawIcon(entry.icon, x + (width - NODE_ICON_SIZE) / 2, y, NODE_ICON_SIZE);
        int currentY = y + NODE_ICON_SIZE + 8;
        String description = this.translateOrEmpty(entry.descriptionKey);
        if (!description.isEmpty()) {
            currentY = this.drawWrappedReturnY(this.cleanModonomiconMarkup(description), x, currentY, width, bottom, MUTED);
        }
        if (page.hasReference()) {
            currentY += 4;
            this.drawWrappedReturnY(I18n.format(page.reference.labelKey), x, currentY, width, bottom, ACCENT);
        }
    }

    private int drawWrappedReturnY(String text, int x, int y, int width, int bottom, int color) {
        List<String> lines = this.getWrappedLines(text, width);
        int currentY = y;
        for (String line : lines) {
            if (currentY + this.fontRenderer.FONT_HEIGHT > bottom) {
                break;
            }
            if (!line.isEmpty()) {
                this.fontRenderer.drawString(line, x, currentY, color);
            }
            currentY += this.fontRenderer.FONT_HEIGHT + 2;
        }
        return currentY;
    }

    private void drawSpotlightPage(GuidePage page, int x, int y, int width, int bottom) {
        ItemStack stack = page.spotlightStack;
        drawRect(x, y, x + SPOTLIGHT_BOX_SIZE, y + SPOTLIGHT_BOX_SIZE, SLOT_EDGE);
        drawRect(x + 1, y + 1, x + SPOTLIGHT_BOX_SIZE - 1, y + SPOTLIGHT_BOX_SIZE - 1, SLOT);

        RenderHelper.enableGUIStandardItemLighting();
        this.mc.getRenderItem().renderItemAndEffectIntoGUI(stack, x + 9, y + 9);
        this.mc.getRenderItem().renderItemOverlays(this.fontRenderer, stack, x + 9, y + 9);
        RenderHelper.disableStandardItemLighting();
        GlStateManager.disableLighting();
        GlStateManager.color(1.0F, 1.0F, 1.0F, 1.0F);
        this.captureHoveredStack(stack, x, y, SPOTLIGHT_BOX_SIZE, SPOTLIGHT_BOX_SIZE);

        int textX = x + SPOTLIGHT_BOX_SIZE + 8;
        int textWidth = Math.max(1, width - SPOTLIGHT_BOX_SIZE - 8);
        List<String> nameLines = this.fontRenderer.listFormattedStringToWidth(stack.getDisplayName(), textWidth);
        int currentY = y + 2;
        for (int i = 0; i < nameLines.size() && i < 2; i++) {
            this.fontRenderer.drawString(nameLines.get(i), textX, currentY, ACCENT);
            currentY += this.fontRenderer.FONT_HEIGHT + 2;
        }
        if (!page.spotlightItemId.isEmpty() && currentY + this.fontRenderer.FONT_HEIGHT <= y + SPOTLIGHT_BOX_SIZE) {
            this.fontRenderer.drawString(page.spotlightItemId, textX, currentY, MUTED);
        }

        this.drawWrappedInBounds(this.getCurrentPageBody(page), x,
                y + SPOTLIGHT_BOX_SIZE + SPOTLIGHT_BODY_SPACING, width, bottom, TEXT);
    }

    private void drawReferencePage(GuidePage page, int x, int y, int width, int bottom) {
        GuideReference reference = page.reference;
        drawRect(x, y, x + width, y + REFERENCE_HEADER_HEIGHT, SLOT);
        drawRect(x, y, x + width, y + 1, SLOT_EDGE);
        drawRect(x, y + REFERENCE_HEADER_HEIGHT - 1, x + width, y + REFERENCE_HEADER_HEIGHT, SLOT_EDGE);

        this.fontRenderer.drawString(I18n.format(reference.labelKey), x + 4, y + 4, ACCENT);
        this.fontRenderer.drawString(reference.id, x + 4, y + 16, MUTED);
        this.drawItemStackRow(reference.previewStacks, x + 4, y + 34, width - 8);

        int bodyY = y + REFERENCE_HEADER_HEIGHT + REFERENCE_BODY_SPACING;
        if (reference.pentaclePreview != null) {
            bodyY = this.drawPentaclePreview(reference.pentaclePreview, x, bodyY, width, bottom);
        }
        if (reference.craftingPreview != null) {
            bodyY = this.drawCraftingPreview(reference.craftingPreview, x, bodyY, width, bottom);
        }
        this.drawWrappedInBounds(this.getCurrentPageBody(page), x, bodyY, width, bottom, TEXT);
    }

    private void drawRitualReference(GuideReference reference, int x, int y, int width, int bottom) {
        RitualDefinition ritual = reference.ritualPreview;
        if (ritual == null) {
            return;
        }

        // Keep this layout aligned with the upstream BookRitualRecipePageRenderer. The 1.12
        // dictionary has no Modonomicon runtime, but the vanilla item renderer can reproduce
        // the same recipe presentation and coordinates.
        int recipeX = x;
        int recipeY = y + 10;
        PentacleDefinition pentacle = findPentacle(removeNamespace(ritual.getPentacleId()));
        String pentacleName = getRitualPentacleName(ritual.getPentacleId(), pentacle);

        RenderHelper.enableGUIStandardItemLighting();
        int ritualCenterX = recipeX + 30;
        int ritualCenterY = recipeY + 70;
        int sacrificialCircleRadius = 30;
        int bowlPaddingVertical = 20;
        int bowlPaddingHorizontal = 15;
        int[][] bowlPositions = {
                {ritualCenterX, ritualCenterY - sacrificialCircleRadius},
                {ritualCenterX + sacrificialCircleRadius, ritualCenterY},
                {ritualCenterX, ritualCenterY + sacrificialCircleRadius},
                {ritualCenterX - sacrificialCircleRadius, ritualCenterY},
                {ritualCenterX + bowlPaddingHorizontal, ritualCenterY - sacrificialCircleRadius},
                {ritualCenterX + sacrificialCircleRadius, ritualCenterY - bowlPaddingVertical},
                {ritualCenterX - bowlPaddingHorizontal, ritualCenterY + sacrificialCircleRadius},
                {ritualCenterX - sacrificialCircleRadius, ritualCenterY + bowlPaddingVertical},
                {ritualCenterX - bowlPaddingHorizontal, ritualCenterY - sacrificialCircleRadius},
                {ritualCenterX + sacrificialCircleRadius, ritualCenterY + bowlPaddingVertical},
                {ritualCenterX + bowlPaddingHorizontal, ritualCenterY + sacrificialCircleRadius},
                {ritualCenterX - sacrificialCircleRadius, ritualCenterY - bowlPaddingVertical}
        };

        List<RitualIngredient> ingredients = ritual.getAdditionalIngredients();
        for (int i = 0; i < ingredients.size() && i < bowlPositions.length; i++) {
            int[] position = bowlPositions[i];
            this.renderRitualItem(new ItemStack(ModBlocks.SACRIFICIAL_BOWL), position[0], position[1]);
            this.renderRitualItem(ingredients.get(i).createDisplayStack(), position[0], position[1] - 5);
        }

        this.renderRitualItem(new ItemStack(ModBlocks.GOLDEN_SACRIFICIAL_BOWL),
                recipeX + 85, recipeY + 110);
        this.renderRitualItem(ritual.getResult().createStack(),
                recipeX + 85, recipeY + 105);
        this.renderRitualItem(ritual.getRitualDummy().createStack(),
                recipeX - 10, recipeY - 5);

        if (!pentacleName.isEmpty()) {
            this.drawScaledStringNoShadow(pentacleName, recipeX + RITUAL_DUMMY_OFFSET, recipeY - 1,
                    0x3366CC, Math.max(1, width - RITUAL_DUMMY_OFFSET - 10));
        }

        if (ritual.requiresItemUse()) {
            this.renderRitualItem(ritual.getItemToUse().createDisplayStack(),
                    recipeX + 50, recipeY + 21);
        }

        this.renderRitualItem(new ItemStack(ModBlocks.GOLDEN_SACRIFICIAL_BOWL),
                recipeX + 30, recipeY + 70);
        this.renderRitualItem(ritual.getActivationIngredient().createDisplayStack(),
                recipeX + 30, recipeY + 65);

        RenderHelper.disableStandardItemLighting();
        GlStateManager.disableLighting();
        GlStateManager.color(1.0F, 1.0F, 1.0F, 1.0F);

        if (ritual.requiresSacrifice()) {
            this.drawScaledStringNoShadow(I18n.format(
                    "gui.occultism.dictionary.reference.sacrifice",
                    ritual.getSacrifice().getDisplayName()),
                    recipeX - 15, recipeY + 15, TEXT, width);
        }
        if (ritual.requiresItemUse()) {
            this.drawScaledStringNoShadow(I18n.format(
                    "gui.occultism.dictionary.reference.item_to_use",
                    ritual.getItemToUse().createDisplayStack().getDisplayName()),
                    recipeX - 15, recipeY + 25, MUTED, width);
        }
        if (ritual.summonsEntity()) {
            this.drawScaledStringNoShadow(I18n.format(
                    "gui.occultism.dictionary.reference.entity",
                    ritual.getEntityToSummon().getDisplayId()),
                    recipeX - 15, recipeY + 120, TEXT, width);
        }
        if (ritual.hasSpiritJobType()) {
            this.drawScaledStringNoShadow(I18n.format(
                    "gui.occultism.dictionary.reference.job",
                    getJobDisplayName(ritual.getSpiritJobType())),
                    recipeX - 15, recipeY + 130, 0x3366CC, width);
        }
    }

    private boolean handleRitualPentacleClick(int mouseX, int mouseY) {
        GuidePage page = this.getCurrentEntries()[this.entryIndex].pages[this.pageIndex];
        if (!page.hasReference() || !page.reference.hasRitualPreview()) {
            return false;
        }

        RitualDefinition ritual = page.reference.ritualPreview;
        String pentacleName = getRitualPentacleName(ritual.getPentacleId(),
                findPentacle(removeNamespace(ritual.getPentacleId())));
        if (pentacleName.isEmpty()) {
            return false;
        }

        int left = (this.width - PANEL_WIDTH) / 2;
        int top = (this.height - PANEL_HEIGHT) / 2;
        int bookLeft = left + (PANEL_WIDTH - BOOK_WIDTH) / 2;
        int bookTop = top + 8;
        int recipeX = bookLeft + BOOK_LEFT_PAGE_X;
        int recipeY = bookTop + BOOK_PAGE_TOP + 10;
        int titleX = recipeX + RITUAL_DUMMY_OFFSET;
        int titleY = recipeY - 1;
        int titleWidth = this.getScaledTextWidth(pentacleName,
                Math.max(1, BOOK_PAGE_WIDTH - RITUAL_DUMMY_OFFSET - 10));
        return mouseX >= titleX && mouseX < titleX + titleWidth
                && mouseY >= titleY && mouseY < titleY + this.fontRenderer.FONT_HEIGHT
                && this.openPentacleEntry(removeNamespace(ritual.getPentacleId()));
    }

    private boolean openPentacleEntry(String pentacleId) {
        for (int category = 0; category < CATEGORIES.length; category++) {
            if (!"pentacles".equals(CATEGORIES[category].id)) {
                continue;
            }
            GuideEntry[] entries = CATEGORIES[category].entries;
            for (int entry = 0; entry < entries.length; entry++) {
                GuideEntry guideEntry = entries[entry];
                if (pentacleId.equals(guideEntry.id) || pentacleId.equals(removeNamespace(guideEntry.fullId))) {
                    this.categoryIndex = category;
                    this.entryIndex = entry;
                    this.pageIndex = 0;
                    this.bodyScroll = 0;
                    this.nodeScrollY = 0;
                    this.initGui();
                    return true;
                }
            }
        }
        return false;
    }

    private int getScaledTextWidth(String text, int maxWidth) {
        int textWidth = this.fontRenderer.getStringWidth(text);
        float scale = Math.min(1.0F, (float) maxWidth / Math.max(1, textWidth));
        return Math.max(1, (int) (textWidth * scale));
    }

    private static String getRitualPentacleName(String pentacleId, PentacleDefinition pentacle) {
        String path = removeNamespace(pentacleId);
        String multiblockKey = "multiblock." + Tags.MOD_ID + "." + path;
        if (I18n.hasKey(multiblockKey)) {
            return I18n.format(multiblockKey);
        }

        String bookKey = "book." + Tags.MOD_ID + ".dictionary_of_spirits.pentacles." + path + ".name";
        if (I18n.hasKey(bookKey)) {
            return I18n.format(bookKey);
        }

        return pentacle == null ? path : pentacle.getDisplayName();
    }

    private void renderRitualItem(ItemStack stack, int x, int y) {
        if (stack.isEmpty()) {
            return;
        }
        this.mc.getRenderItem().renderItemAndEffectIntoGUI(stack, x, y);
        this.mc.getRenderItem().renderItemOverlays(this.fontRenderer, stack, x, y);
        this.captureHoveredStack(stack, x, y, 16, 16);
    }

    private void drawCenteredFitting(String text, int centerX, int y, int maxWidth, int color) {
        if (text == null || text.isEmpty() || maxWidth <= 0) {
            return;
        }
        float scale = Math.min(1.0F, (float) maxWidth / Math.max(1, this.fontRenderer.getStringWidth(text)));
        GlStateManager.pushMatrix();
        GlStateManager.translate(centerX, y, 0.0F);
        GlStateManager.scale(scale, scale, 1.0F);
        this.drawCenteredString(this.fontRenderer, text, 0, 0, color);
        GlStateManager.popMatrix();
    }

    private void drawScaledStringNoShadow(String text, int x, int y, int color, int maxWidth) {
        if (text == null || text.isEmpty() || maxWidth <= 0) {
            return;
        }
        int textWidth = this.fontRenderer.getStringWidth(text);
        float scale = Math.min(1.0F, (float) maxWidth / Math.max(1, textWidth));
        GlStateManager.pushMatrix();
        if (scale < 1.0F) {
            GlStateManager.translate(x - x * scale, y - y * scale, 0.0F);
            GlStateManager.scale(scale, scale, scale);
        }
        this.fontRenderer.drawString(text, x, y + (int) (this.fontRenderer.FONT_HEIGHT * (1.0F - scale)),
                color);
        GlStateManager.popMatrix();
    }

    private int drawCraftingPreview(CraftingGridPreview preview, int x, int y, int width, int bottom) {
        int gridWidth = CRAFTING_GRID_SIZE * REFERENCE_SLOT_SIZE
                + (CRAFTING_GRID_SIZE - 1) * CRAFTING_SLOT_SPACING;
        int gridHeight = gridWidth;
        int totalWidth = gridWidth + CRAFTING_PREVIEW_SPACING + CRAFTING_ARROW_WIDTH
                + CRAFTING_PREVIEW_SPACING + REFERENCE_SLOT_SIZE;
        if (bottom - y < gridHeight) {
            return y;
        }

        int startX = x + Math.max(0, (width - totalWidth) / 2);
        int startY = y;
        RenderHelper.enableGUIStandardItemLighting();
        for (int row = 0; row < CRAFTING_GRID_SIZE; row++) {
            for (int column = 0; column < CRAFTING_GRID_SIZE; column++) {
                int slot = row * CRAFTING_GRID_SIZE + column;
                int slotX = startX + column * (REFERENCE_SLOT_SIZE + CRAFTING_SLOT_SPACING);
                int slotY = startY + row * (REFERENCE_SLOT_SIZE + CRAFTING_SLOT_SPACING);
                this.drawItemSlot(preview.slots[slot], slotX, slotY);
            }
        }

        int arrowX = startX + gridWidth + CRAFTING_PREVIEW_SPACING;
        int arrowY = startY + (gridHeight - this.fontRenderer.FONT_HEIGHT) / 2;
        this.fontRenderer.drawString(">", arrowX + 4, arrowY, MUTED);

        int resultX = arrowX + CRAFTING_ARROW_WIDTH + CRAFTING_PREVIEW_SPACING;
        int resultY = startY + (gridHeight - REFERENCE_SLOT_SIZE) / 2;
        this.drawItemSlot(preview.result, resultX, resultY);
        RenderHelper.disableStandardItemLighting();
        GlStateManager.disableLighting();
        GlStateManager.color(1.0F, 1.0F, 1.0F, 1.0F);
        return y + gridHeight + REFERENCE_BODY_SPACING;
    }

    private int drawPentaclePreview(PentacleDefinition pentacle, int x, int y, int width, int bottom) {
        String[] pattern = pentacle.getPattern();
        int gridWidth = pentacle.getWidth();
        int gridHeight = pentacle.getHeight();
        int availableHeight = Math.min(PENTACLE_PREVIEW_MAX_HEIGHT, Math.max(0, bottom - y - PENTACLE_PREVIEW_SPACING));
        if (gridWidth <= 0 || gridHeight <= 0 || availableHeight <= 0) {
            return y;
        }

        int cell = Math.max(2, Math.min(width / gridWidth, availableHeight / gridHeight));
        int previewWidth = cell * gridWidth;
        int previewHeight = cell * gridHeight;
        int startX = x + Math.max(0, (width - previewWidth) / 2);
        drawRect(startX - 1, y - 1, startX + previewWidth + 1, y + previewHeight + 1, SLOT_EDGE);
        drawRect(startX, y, startX + previewWidth, y + previewHeight, PANEL);

        for (int row = 0; row < pattern.length; row++) {
            String line = pattern[row];
            for (int column = 0; column < line.length(); column++) {
                char symbol = line.charAt(column);
                int color = getPentacleSymbolColor(symbol);
                if (color == 0) {
                    continue;
                }
                int cellX = startX + column * cell;
                int cellY = y + row * cell;
                drawRect(cellX + 1, cellY + 1, cellX + cell - 1, cellY + cell - 1, color);
            }
        }

        return y + previewHeight + PENTACLE_PREVIEW_SPACING;
    }

    private static int getPentacleSymbolColor(char symbol) {
        switch (symbol) {
            case '0':
                return 0xFFFFD36A;
            case 'W':
                return 0xFFEFEFEF;
            case 'S':
                return 0xFFBFC3C7;
            case 'G':
                return 0xFF85898E;
            case 'K':
                return 0xFF2D3034;
            case 'B':
                return 0xFF8A5A32;
            case 'R':
                return 0xFFCC3838;
            case 'O':
                return 0xFFE87A2C;
            case 'Y':
                return 0xFFE8D34B;
            case 'L':
                return 0xFF8DD43B;
            case 'E':
                return 0xFF3D9D54;
            case 'C':
                return 0xFF39BFC2;
            case 'A':
                return 0xFF83BDEB;
            case 'U':
                return 0xFF3F65D4;
            case 'X':
                return 0xFF7A4CCE;
            case 'M':
                return 0xFFC94FC8;
            case 'P':
                return 0xFFE58BB8;
            case '1':
                return 0xFFFFE59B;
            case '2':
                return 0xFFE4E1DA;
            case '3':
                return 0xFF9AD9FF;
            case '4':
                return 0xFFB8A890;
            default:
                return 0;
        }
    }

    private void drawItemStackRow(List<ItemStack> stacks, int x, int y, int width) {
        if (stacks.isEmpty()) {
            return;
        }
        int maxSlots = Math.max(1, width / (REFERENCE_SLOT_SIZE + REFERENCE_SLOT_SPACING));
        int count = Math.min(stacks.size(), maxSlots);
        RenderHelper.enableGUIStandardItemLighting();
        for (int i = 0; i < count; i++) {
            ItemStack stack = stacks.get(i);
            int slotX = x + i * (REFERENCE_SLOT_SIZE + REFERENCE_SLOT_SPACING);
            this.drawItemSlot(stack, slotX, y);
        }
        RenderHelper.disableStandardItemLighting();
        GlStateManager.disableLighting();
        GlStateManager.color(1.0F, 1.0F, 1.0F, 1.0F);
    }

    private void drawItemSlot(ItemStack stack, int x, int y) {
        drawRect(x, y, x + REFERENCE_SLOT_SIZE, y + REFERENCE_SLOT_SIZE, SLOT_EDGE);
        drawRect(x + 1, y + 1, x + REFERENCE_SLOT_SIZE - 1, y + REFERENCE_SLOT_SIZE - 1, PANEL);
        if (!stack.isEmpty()) {
            this.mc.getRenderItem().renderItemAndEffectIntoGUI(stack, x + 1, y + 1);
            this.mc.getRenderItem().renderItemOverlays(this.fontRenderer, stack, x + 1, y + 1);
            this.captureHoveredStack(stack, x, y, REFERENCE_SLOT_SIZE, REFERENCE_SLOT_SIZE);
        }
    }

    private void captureHoveredStack(ItemStack stack, int x, int y, int width, int height) {
        if (stack.isEmpty() || !this.hoveredStack.isEmpty()) {
            return;
        }
        if (this.currentMouseX >= x && this.currentMouseX < x + width
                && this.currentMouseY >= y && this.currentMouseY < y + height) {
            this.hoveredStack = stack.copy();
        }
    }

    private void drawHoveredStackTooltip(int mouseX, int mouseY) {
        if (this.hoveredStack.isEmpty()) {
            return;
        }
        this.renderToolTip(this.hoveredStack, mouseX, mouseY);
        GlStateManager.disableLighting();
        GlStateManager.color(1.0F, 1.0F, 1.0F, 1.0F);
    }

    private void drawImagePage(GuidePage page, int x, int y, int width, int bottom) {
        String text = this.getCurrentPageBody(page);
        List<String> lines = this.getWrappedLines(text, width);
        int currentY = y;
        int totalHeight = bottom - y;
        int imageMaxHeight = this.getImageMaxHeight(page, totalHeight, lines.isEmpty());
        if (!lines.isEmpty()) {
            int reservedHeight = this.getPreferredImageHeight(page, width, imageMaxHeight);
            int textBottom = Math.max(y, bottom - reservedHeight - IMAGE_SPACING);
            for (int i = this.bodyScroll; i < lines.size()
                    && currentY + this.fontRenderer.FONT_HEIGHT <= textBottom; i++) {
                String line = lines.get(i);
                if (!line.isEmpty()) {
                    this.fontRenderer.drawString(line, x, currentY, TEXT);
                }
                currentY += this.fontRenderer.FONT_HEIGHT + 2;
            }
            currentY += IMAGE_SPACING;
        }

        int availableHeight = Math.max(0, bottom - currentY);
        if (availableHeight <= 0) {
            return;
        }
        availableHeight = Math.min(availableHeight, imageMaxHeight);

        if (page.images.length == 1) {
            this.drawScaledImage(page.images[0], x, currentY, width, availableHeight, true, lines.isEmpty());
            return;
        }

        int imageCount = page.images.length;
        int maxImageWidth = Math.max(1, (width - IMAGE_SPACING * (imageCount - 1)) / imageCount);
        int totalWidth = 0;
        int rowHeight = 0;
        int[] widths = new int[imageCount];
        int[] heights = new int[imageCount];
        for (int i = 0; i < imageCount; i++) {
            int[] size = this.getScaledSize(page.images[i], maxImageWidth, availableHeight);
            widths[i] = size[0];
            heights[i] = size[1];
            totalWidth += widths[i];
            rowHeight = Math.max(rowHeight, heights[i]);
        }
        totalWidth += IMAGE_SPACING * (imageCount - 1);

        int drawX = x + Math.max(0, (width - totalWidth) / 2);
        int drawY = currentY + Math.max(0, (availableHeight - rowHeight) / 2);
        for (int i = 0; i < imageCount; i++) {
            this.drawImage(page.images[i], drawX, drawY + Math.max(0, (rowHeight - heights[i]) / 2),
                    widths[i], heights[i]);
            drawX += widths[i] + IMAGE_SPACING;
        }
    }

    private int getPreferredImageHeight(GuidePage page, int width, int availableHeight) {
        if (page.images.length == 0) {
            return 0;
        }
        if (page.images.length == 1) {
            int[] size = this.getScaledSize(page.images[0], width, availableHeight);
            return size[1];
        }
        int maxImageWidth = Math.max(1, (width - IMAGE_SPACING * (page.images.length - 1)) / page.images.length);
        int rowHeight = 0;
        for (GuideImage image : page.images) {
            rowHeight = Math.max(rowHeight, this.getScaledSize(image, maxImageWidth, availableHeight)[1]);
        }
        return rowHeight;
    }

    private int getImageMaxHeight(GuidePage page, int availableHeight, boolean imageOnly) {
        if (!page.hasImages()) {
            return 0;
        }
        if (imageOnly) {
            return availableHeight;
        }
        return Math.max(36, availableHeight / 2);
    }

    private void drawScaledImage(GuideImage image, int x, int y, int maxWidth, int maxHeight,
            boolean centerX, boolean centerY) {
        int[] size = this.getScaledSize(image, maxWidth, maxHeight);
        int drawX = centerX ? x + Math.max(0, (maxWidth - size[0]) / 2) : x;
        int drawY = centerY ? y + Math.max(0, (maxHeight - size[1]) / 2) : y;
        this.drawImage(image, drawX, drawY, size[0], size[1]);
    }

    private int[] getScaledSize(GuideImage image, int maxWidth, int maxHeight) {
        float scale = Math.min((float) maxWidth / image.width, (float) maxHeight / image.height);
        scale = Math.min(1.0F, Math.max(0.01F, scale));
        return new int[] {
            Math.max(1, Math.round(image.width * scale)),
            Math.max(1, Math.round(image.height * scale))
        };
    }

    private void drawImage(GuideImage image, int x, int y, int width, int height) {
        GlStateManager.color(1.0F, 1.0F, 1.0F, 1.0F);
        GlStateManager.enableBlend();
        this.mc.getTextureManager().bindTexture(image.location);
        drawModalRectWithCustomSizedTexture(x, y, 0.0F, 0.0F, width, height,
                image.width, image.height);
        GlStateManager.disableBlend();
    }

    private void drawWrapped(String text, int x, int y, int width, int color) {
        this.drawWrappedInBounds(text, x, y, width,
                (this.height - PANEL_HEIGHT) / 2 + PANEL_HEIGHT - BODY_BOTTOM_MARGIN, color);
    }

    private void drawWrappedInBounds(String text, int x, int y, int width, int bottom, int color) {
        List<String> lines = this.getWrappedLines(text, width);
        int currentY = y;
        for (int i = this.bodyScroll; i < lines.size() && currentY + this.fontRenderer.FONT_HEIGHT <= bottom; i++) {
            String line = lines.get(i);
            if (!line.isEmpty()) {
                this.fontRenderer.drawString(line, x, currentY, color);
            }
            currentY += this.fontRenderer.FONT_HEIGHT + 2;
        }
    }

    private void updateNavigationButtons() {
        for (GuiButton button : this.buttonList) {
            if (button.id == ID_PREVIOUS) {
                button.enabled = this.isEntryPage()
                        ? this.entryIndex > 0 || this.pageIndex > 0
                        : this.nodeScrollY > 0;
            } else if (button.id == ID_NEXT) {
                button.enabled = this.isEntryPage()
                        ? this.entryIndex < this.getCurrentEntries().length - 1
                                || this.pageIndex < this.getCurrentEntries()[this.entryIndex].pages.length - 1
                        : this.nodeScrollY < this.getMaxListRowOffset();
            } else if (button.id == ID_CONTENTS) {
                button.enabled = this.isEntryPage();
            }
        }
    }

    private void handlePrevious() {
        if (this.isEntryPage() && this.pageIndex > 0) {
            this.pageIndex--;
            this.bodyScroll = 0;
            this.initGui();
        } else if (this.isEntryPage() && this.entryIndex > 0) {
            this.entryIndex--;
            this.pageIndex = this.getCurrentEntries()[this.entryIndex].pages.length - 1;
            this.bodyScroll = 0;
            this.initGui();
        } else if (!this.isEntryPage() && this.nodeScrollY > 0) {
            this.nodeScrollY = Math.max(0, this.nodeScrollY - NODE_SCROLL_STEP);
            this.initGui();
        }
    }

    private void handleNext() {
        if (this.isEntryPage()
                && this.pageIndex < this.getCurrentEntries()[this.entryIndex].pages.length - 1) {
            this.pageIndex++;
            this.bodyScroll = 0;
            this.initGui();
        } else if (this.isEntryPage() && this.entryIndex < this.getCurrentEntries().length - 1) {
            this.entryIndex++;
            this.pageIndex = 0;
            this.bodyScroll = 0;
            this.initGui();
        } else if (!this.isEntryPage() && this.nodeScrollY < this.getMaxListRowOffset()) {
            this.nodeScrollY = Math.min(this.getMaxListRowOffset(),
                    this.nodeScrollY + NODE_SCROLL_STEP);
            this.initGui();
        }
    }

    private void handleContents() {
        if (this.isEntryPage()) {
            GuideEntry entry = this.getCurrentEntries()[this.entryIndex];
            this.entryIndex = NO_ENTRY;
            this.pageIndex = 0;
            this.bodyScroll = 0;
            this.nodeScrollY = Math.max(0, Math.min(entry.y * NODE_SPACING, this.getMaxListRowOffset()));
            this.initGui();
        }
    }

    private List<String> getWrappedLines(String text, int width) {
        List<String> lines = new ArrayList<>();
        String[] paragraphs = text.split("\\\\n|\\n", -1);
        for (String paragraph : paragraphs) {
            if (paragraph.trim().isEmpty()) {
                lines.add("");
                continue;
            }
            lines.addAll(this.fontRenderer.listFormattedStringToWidth(paragraph, width));
            lines.add("");
        }
        if (!lines.isEmpty() && lines.get(lines.size() - 1).isEmpty()) {
            lines.remove(lines.size() - 1);
        }
        return lines;
    }

    private int getMaxListRowOffset() {
        this.ensureCategorySelected();
        GuideCategory category = CATEGORIES[this.categoryIndex];
        int graphHeight = (category.maxY - category.minY + 1) * NODE_SPACING + NODE_SIZE;
        return Math.max(0, graphHeight - GRAPH_HEIGHT);
    }

    private int getMaxBodyScroll() {
        if (!this.isEntryPage()) {
            return 0;
        }
        int visibleLines = (BOOK_PAGE_BOTTOM - BOOK_PAGE_TOP) / (this.fontRenderer.FONT_HEIGHT + 2);
        GuidePage page = this.getCurrentEntries()[this.entryIndex].pages[this.pageIndex];
        if (page.hasReference() && page.reference.hasRitualPreview()) {
            return 0;
        }
        List<String> lines = this.getWrappedLines(this.getCurrentPageBody(page), BODY_WIDTH);
        if (page.hasImages()) {
            int bodyHeight = BOOK_PAGE_BOTTOM - BOOK_PAGE_TOP;
            int reservedHeight = this.getPreferredImageHeight(page, BODY_WIDTH,
                    this.getImageMaxHeight(page, bodyHeight, lines.isEmpty()));
            visibleLines = Math.max(1, (BOOK_PAGE_BOTTOM - BOOK_PAGE_TOP - reservedHeight
                    - IMAGE_SPACING) / (this.fontRenderer.FONT_HEIGHT + 2));
        } else if (page.hasSpotlight()) {
            visibleLines = Math.max(1, (BOOK_PAGE_BOTTOM - BOOK_PAGE_TOP - SPOTLIGHT_BOX_SIZE
                    - SPOTLIGHT_BODY_SPACING) / (this.fontRenderer.FONT_HEIGHT + 2));
        } else if (page.hasReference()) {
            visibleLines = Math.max(1, (BOOK_PAGE_BOTTOM - BOOK_PAGE_TOP - REFERENCE_HEADER_HEIGHT
                    - REFERENCE_BODY_SPACING) / (this.fontRenderer.FONT_HEIGHT + 2));
            if (page.reference.hasPentaclePreview()) {
                visibleLines = Math.max(1, visibleLines
                        - (PENTACLE_PREVIEW_MAX_HEIGHT + PENTACLE_PREVIEW_SPACING)
                        / (this.fontRenderer.FONT_HEIGHT + 2));
            }
            if (page.reference.hasCraftingPreview()) {
                int craftingHeight = CRAFTING_GRID_SIZE * REFERENCE_SLOT_SIZE
                        + (CRAFTING_GRID_SIZE - 1) * CRAFTING_SLOT_SPACING
                        + REFERENCE_BODY_SPACING;
                visibleLines = Math.max(1, visibleLines
                        - craftingHeight / (this.fontRenderer.FONT_HEIGHT + 2));
            }
        }
        return Math.max(0, lines.size() - visibleLines);
    }

    private void clampBodyScroll() {
        this.bodyScroll = Math.max(0, Math.min(this.bodyScroll, this.getMaxBodyScroll()));
    }

    private void ensureCategorySelected() {
        if (this.categoryIndex < 0 || this.categoryIndex >= CATEGORIES.length) {
            this.categoryIndex = getDefaultCategoryIndex();
            this.entryIndex = NO_ENTRY;
            this.pageIndex = 0;
            this.nodeScrollY = 0;
            this.bodyScroll = 0;
        }
    }

    private boolean isCategoryContents() {
        return this.categoryIndex != NO_CATEGORY && this.entryIndex == NO_ENTRY;
    }

    private boolean isEntryPage() {
        return this.categoryIndex != NO_CATEGORY && this.entryIndex != NO_ENTRY;
    }

    private GuideEntry[] getCurrentEntries() {
        this.ensureCategorySelected();
        return CATEGORIES[this.categoryIndex].entries;
    }

    private static int getDefaultCategoryIndex() {
        return CATEGORIES.length == 0 ? NO_CATEGORY : 0;
    }

    private static GuideCategory category(String id, GuideEntry... entries) {
        return new GuideCategory(id, "guide.occultism.dictionary.category." + id + ".title",
                "", GuideIcon.item(Tags.MOD_ID + ":dictionary_of_spirits_icon"),
                defaultNodeBackground("square_gray"), entries);
    }

    private String getCurrentPageBody(GuidePage page) {
        List<String> sections = new ArrayList<>();
        String title = this.translateOrEmpty(page.titleKey);
        if (!title.isEmpty() && !title.equals(I18n.format(this.getCurrentEntries()[this.entryIndex].titleKey))) {
            sections.add(title);
        }
        String body = this.translateOrEmpty(page.bodyKey);
        if (!body.isEmpty()) {
            sections.add(body);
        }
        String referenceBody = page.getReferenceBody();
        if (!referenceBody.isEmpty()) {
            sections.add(referenceBody);
        }
        String detail = page.getTextDetail();
        if (!detail.isEmpty()) {
            sections.add(detail);
        }
        if (sections.isEmpty()) {
            return page.hasImages() ? "" : I18n.format(this.getCurrentEntries()[this.entryIndex].titleKey);
        }
        return this.cleanModonomiconMarkup(String.join("\n\n", sections));
    }

    private String translateOrEmpty(String key) {
        if (key == null || key.isEmpty()) {
            return "";
        }
        String translated = I18n.format(key);
        return translated.equals(key) ? "" : translated;
    }

    private String cleanModonomiconMarkup(String text) {
        return text.replace("\\\\", "\n")
                .replace("\\n", "\n")
                .replaceAll("\\[#\\]\\([0-9a-fA-F]+\\)(.*?)\\[#\\]\\(\\)", "$1")
                .replaceAll("\\[([^\\]]*)\\]\\([^)]*\\)", "$1")
                .replace("**", "")
                .replace("_", " ");
    }

    private static GuideEntry entry(String id) {
        return new GuideEntry(id, id, "guide.occultism.dictionary." + id + ".title", "",
                GuideIcon.EMPTY, defaultNodeBackground("square_gray"), 0, 0, new String[0],
                new GuidePage("", "guide.occultism.dictionary." + id + ".body", ""));
    }

    private static final class GuideCategory {
        private final String id;
        private final String titleKey;
        private final String descriptionKey;
        private final GuideIcon icon;
        private final ResourceLocation nodeBackground;
        private final GuideEntry[] entries;
        private final int minX;
        private final int maxX;
        private final int minY;
        private final int maxY;

        private GuideCategory(String id, String titleKey, String descriptionKey, GuideIcon icon,
                ResourceLocation nodeBackground, GuideEntry[] entries) {
            this.id = id == null ? "" : id;
            this.titleKey = titleKey;
            this.descriptionKey = descriptionKey == null ? "" : descriptionKey;
            this.icon = icon == null ? GuideIcon.EMPTY : icon;
            this.nodeBackground = nodeBackground == null ? defaultNodeBackground("square_gray") : nodeBackground;
            this.entries = entries;
            int lowX = 0;
            int highX = 0;
            int lowY = 0;
            int highY = 0;
            if (entries.length > 0) {
                lowX = entries[0].x;
                highX = entries[0].x;
                lowY = entries[0].y;
                highY = entries[0].y;
                for (GuideEntry entry : entries) {
                    lowX = Math.min(lowX, entry.x);
                    highX = Math.max(highX, entry.x);
                    lowY = Math.min(lowY, entry.y);
                    highY = Math.max(highY, entry.y);
                }
            }
            this.minX = lowX;
            this.maxX = highX;
            this.minY = lowY;
            this.maxY = highY;
        }
    }

    private static final class GuideEntry {
        private final String id;
        private final String fullId;
        private final String titleKey;
        private final String descriptionKey;
        private final GuideIcon icon;
        private final ResourceLocation nodeBackground;
        private final int x;
        private final int y;
        private final String[] parents;
        private final GuidePage[] pages;

        private GuideEntry(String id, String fullId, String titleKey, String descriptionKey, GuideIcon icon,
                ResourceLocation nodeBackground, int x, int y, String[] parents, GuidePage... pages) {
            this.id = id == null ? "" : id;
            this.fullId = fullId == null ? this.id : fullId;
            this.titleKey = titleKey;
            this.descriptionKey = descriptionKey == null ? "" : descriptionKey;
            this.icon = icon == null ? GuideIcon.EMPTY : icon;
            this.nodeBackground = nodeBackground == null ? defaultNodeBackground("square_gray") : nodeBackground;
            this.x = x;
            this.y = y;
            this.parents = parents == null ? new String[0] : parents;
            this.pages = pages.length == 0 ? new GuidePage[] { new GuidePage("", titleKey, "") } : pages;
        }
    }

    private static final class GuideIcon {
        private static final GuideIcon EMPTY = new GuideIcon(ItemStack.EMPTY, null, 16, 16);

        private final ItemStack stack;
        private final ResourceLocation texture;
        private final int width;
        private final int height;

        private GuideIcon(ItemStack stack, ResourceLocation texture, int width, int height) {
            this.stack = stack == null ? ItemStack.EMPTY : stack;
            this.texture = texture;
            this.width = Math.max(1, width);
            this.height = Math.max(1, height);
        }

        private static GuideIcon item(String itemId) {
            ItemStack stack = parseIconStack(itemId);
            if (!stack.isEmpty()) {
                return new GuideIcon(stack, null, 16, 16);
            }
            if ("occultism:dictionary_of_spirits_icon".equals(itemId)) {
                return new GuideIcon(ItemStack.EMPTY,
                        new ResourceLocation(Tags.MOD_ID, "textures/item/dictionary_of_spirits.png"), 16, 16);
            }
            return EMPTY;
        }

        private static GuideIcon texture(ResourceLocation location, int width, int height) {
            return new GuideIcon(ItemStack.EMPTY, location, width, height);
        }
    }

    private static final class GuidePage {
        private final String titleKey;
        private final String bodyKey;
        private final String type;
        private final String detail;
        private final GuideImage[] images;
        private final String spotlightItemId;
        private final ItemStack spotlightStack;
        private final GuideReference reference;

        private GuidePage(String titleKey, String bodyKey, String detail) {
            this("", titleKey, bodyKey, detail);
        }

        private GuidePage(String type, String titleKey, String bodyKey, String detail) {
            this.type = type == null ? "" : type;
            this.titleKey = titleKey == null ? "" : titleKey;
            this.bodyKey = bodyKey == null ? "" : bodyKey;
            this.detail = detail == null ? "" : detail;
            this.images = parseImages(this.detail);
            this.spotlightItemId = parseSpotlightItemId(this.type, this.detail);
            this.spotlightStack = parseSpotlightStack(this.spotlightItemId);
            this.reference = parseReference(this.type, this.detail);
        }

        private boolean hasImages() {
            return this.images.length > 0;
        }

        private boolean isSpotlight() {
            return "modonomicon:spotlight".equals(this.type);
        }

        private boolean hasSpotlight() {
            return !this.spotlightStack.isEmpty();
        }

        private boolean hasReference() {
            return this.reference != null;
        }

        private String getReferenceBody() {
            return this.hasReference() ? this.reference.getBody() : "";
        }

        private String getTextDetail() {
            if (this.hasImages()) {
                return "";
            }
            if (this.isSpotlight() && this.hasSpotlight()) {
                return "";
            }
            if (this.hasReference()) {
                return "";
            }
            return this.detail;
        }
    }

    private static final class GuideReference {
        private final String labelKey;
        private final String id;
        private final List<GuideReferenceLine> lines;
        private final List<ItemStack> previewStacks;
        private final PentacleDefinition pentaclePreview;
        private final CraftingGridPreview craftingPreview;
        private final RitualDefinition ritualPreview;

        private GuideReference(String labelKey, String id, List<GuideReferenceLine> lines,
                List<ItemStack> previewStacks, PentacleDefinition pentaclePreview,
                CraftingGridPreview craftingPreview, RitualDefinition ritualPreview) {
            this.labelKey = labelKey;
            this.id = id;
            this.lines = lines;
            this.previewStacks = previewStacks;
            this.pentaclePreview = pentaclePreview;
            this.craftingPreview = craftingPreview;
            this.ritualPreview = ritualPreview;
        }

        private boolean hasPentaclePreview() {
            return this.pentaclePreview != null;
        }

        private boolean hasCraftingPreview() {
            return this.craftingPreview != null;
        }

        private boolean hasRitualPreview() {
            return this.ritualPreview != null;
        }

        private String getBody() {
            List<String> text = new ArrayList<>();
            for (GuideReferenceLine line : this.lines) {
                if (line.value.isEmpty()) {
                    text.add(I18n.format(line.labelKey));
                } else {
                    text.add(I18n.format(line.labelKey, line.value));
                }
            }
            return String.join("\n", text);
        }
    }

    private static final class GuideReferenceLine {
        private final String labelKey;
        private final String value;

        private GuideReferenceLine(String labelKey) {
            this(labelKey, "");
        }

        private GuideReferenceLine(String labelKey, String value) {
            this.labelKey = labelKey;
            this.value = value == null ? "" : value;
        }
    }

    private static final class EntityReferenceMapping {
        private final String localId;
        private final String noteKey;

        private EntityReferenceMapping(String localId, String noteKey) {
            this.localId = localId;
            this.noteKey = noteKey;
        }
    }

    private static final class CraftingGridPreview {
        private final ItemStack[] slots;
        private final ItemStack result;

        private CraftingGridPreview(ItemStack[] slots, ItemStack result) {
            this.slots = slots;
            this.result = result;
        }
    }

    private static final class GuideImage {
        private final ResourceLocation location;
        private final int width;
        private final int height;

        private GuideImage(ResourceLocation location, int width, int height) {
            this.location = location;
            this.width = width;
            this.height = height;
        }
    }

    private static class InvisibleButton extends GuiButton {

        private InvisibleButton(int buttonId, int x, int y, int width, int height, String label) {
            super(buttonId, x, y, width, height, label);
        }

        @Override
        public void drawButton(Minecraft mc, int mouseX, int mouseY, float partialTicks) {
            this.hovered = mouseX >= this.x && mouseY >= this.y
                    && mouseX < this.x + this.width && mouseY < this.y + this.height;
        }
    }

    private static final class IconButton extends GuiButton {
        private final ResourceLocation texture;
        private final int textureWidth;
        private final int textureHeight;

        private IconButton(int buttonId, int x, int y, int width, int height, ResourceLocation texture,
                int textureWidth, int textureHeight, String label) {
            super(buttonId, x, y, width, height, label);
            this.texture = texture;
            this.textureWidth = textureWidth;
            this.textureHeight = textureHeight;
        }

        @Override
        public void drawButton(Minecraft mc, int mouseX, int mouseY, float partialTicks) {
            if (!this.visible) {
                return;
            }
            this.hovered = mouseX >= this.x && mouseY >= this.y
                    && mouseX < this.x + this.width && mouseY < this.y + this.height;
            GlStateManager.color(this.enabled ? 1.0F : 0.45F, this.enabled ? 1.0F : 0.45F,
                    this.enabled ? 1.0F : 0.45F, 1.0F);
            mc.getTextureManager().bindTexture(this.texture);
            Gui.drawModalRectWithCustomSizedTexture(this.x, this.y, 0.0F, 0.0F,
                    this.width, this.height, this.textureWidth, this.textureHeight);
            GlStateManager.color(1.0F, 1.0F, 1.0F, 1.0F);
        }
    }

    private static GuideCategory[] loadCategories() {
        try (InputStream stream = GuiDictionaryOfSpirits.class.getClassLoader().getResourceAsStream(UPSTREAM_INDEX)) {
            if (stream == null) {
                return fallbackCategories();
            }
            JsonObject root = new JsonParser().parse(new InputStreamReader(stream, StandardCharsets.UTF_8))
                    .getAsJsonObject();
            JsonArray categories = root.getAsJsonArray("categories");
            List<GuideCategory> loaded = new ArrayList<>();
            for (JsonElement categoryElement : categories) {
                JsonObject categoryJson = categoryElement.getAsJsonObject();
                JsonArray entriesJson = categoryJson.getAsJsonArray("entries");
                List<GuideEntry> entries = new ArrayList<>();
                for (JsonElement entryElement : entriesJson) {
                    JsonObject entryJson = entryElement.getAsJsonObject();
                    JsonArray pagesJson = entryJson.getAsJsonArray("pages");
                    List<GuidePage> pages = new ArrayList<>();
                    for (JsonElement pageElement : pagesJson) {
                        JsonObject pageJson = pageElement.getAsJsonObject();
                        pages.add(new GuidePage(getString(pageJson, "type"), getString(pageJson, "title"),
                                getString(pageJson, "text"),
                                getString(pageJson, "detail")));
                    }
                    String entryId = getString(entryJson, "id");
                    String categoryId = getString(categoryJson, "id");
                    entries.add(new GuideEntry(entryId, Tags.MOD_ID + ":" + categoryId + "/" + entryId,
                            getString(entryJson, "name"),
                            getString(entryJson, "description"),
                            parseIcon(entryJson.get("icon")),
                            parseNodeBackground(entryJson.get("background")),
                            getInt(entryJson, "x", 0),
                            getInt(entryJson, "y", 0),
                            parseParents(entryJson.get("parents")),
                            pages.toArray(new GuidePage[0])));
                }
                loaded.add(new GuideCategory(getString(categoryJson, "id"),
                        getString(categoryJson, "name"),
                        getString(categoryJson, "description"),
                        parseIcon(categoryJson.get("icon")),
                        defaultNodeBackground("square_gray"),
                        entries.toArray(new GuideEntry[0])));
            }
            return loaded.isEmpty() ? fallbackCategories() : loaded.toArray(new GuideCategory[0]);
        } catch (Exception e) {
            Occultism.LOGGER.error("Failed to load upstream Dictionary of Spirits index.", e);
            return fallbackCategories();
        }
    }

    private static String getString(JsonObject json, String key) {
        return json.has(key) && !json.get(key).isJsonNull() ? json.get(key).getAsString() : "";
    }

    private static GuideIcon parseIcon(JsonElement element) {
        if (element == null || element.isJsonNull()) {
            return GuideIcon.EMPTY;
        }
        if (element.isJsonPrimitive()) {
            return GuideIcon.item(element.getAsString());
        }
        JsonObject icon = element.getAsJsonObject();
        if (icon.has("texture")) {
            ResourceLocation texture = new ResourceLocation(icon.get("texture").getAsString());
            int width = getInt(icon, "width", 16);
            int height = getInt(icon, "height", 16);
            return GuideIcon.texture(texture, width, height);
        }
        if (icon.has("id")) {
            return GuideIcon.item(icon.get("id").getAsString());
        }
        return GuideIcon.EMPTY;
    }

    private static ItemStack parseIconStack(String itemId) {
        if ("occultism:dictionary_of_spirits_icon".equals(itemId)) {
            return new ItemStack(ModItems.DICTIONARY_OF_SPIRITS);
        }
        return parseSpotlightStack(itemId);
    }

    private static ResourceLocation parseNodeBackground(JsonElement element) {
        if (element == null || element.isJsonNull() || !element.isJsonObject()) {
            return defaultNodeBackground("square_gray");
        }
        JsonObject background = element.getAsJsonObject();
        String sprite = getString(background, "sprite");
        if (sprite.isEmpty()) {
            return defaultNodeBackground("square_gray");
        }
        return spriteToTexture(sprite);
    }

    private static ResourceLocation defaultNodeBackground(String name) {
        return new ResourceLocation("modonomicon",
                "textures/gui/sprites/modonomicon/themes/default/node/entry_backgrounds/" + name + ".png");
    }

    private static ResourceLocation spriteToTexture(String sprite) {
        String normalized = sprite;
        if (normalized.startsWith("modonomicon:modonomicon/themes/default/node/entry_backgrounds/")) {
            normalized = normalized.substring("modonomicon:modonomicon/themes/default/node/entry_backgrounds/".length());
            return defaultNodeBackground(normalized);
        }
        ResourceLocation location = new ResourceLocation(sprite);
        return new ResourceLocation(location.getNamespace(), "textures/gui/sprites/" + location.getPath() + ".png");
    }

    private static String[] parseParents(JsonElement element) {
        if (element == null || element.isJsonNull() || !element.isJsonArray()) {
            return new String[0];
        }
        List<String> parents = new ArrayList<>();
        for (JsonElement parentElement : element.getAsJsonArray()) {
            if (!parentElement.isJsonObject()) {
                continue;
            }
            String parent = getString(parentElement.getAsJsonObject(), "entry");
            if (!parent.isEmpty()) {
                parents.add(parent);
            }
        }
        return parents.toArray(new String[0]);
    }

    private static GuideImage[] parseImages(String detail) {
        if (detail == null || !detail.startsWith("Image: ")) {
            return new GuideImage[0];
        }
        List<GuideImage> images = new ArrayList<>();
        String[] refs = detail.substring("Image: ".length()).split(",");
        for (String ref : refs) {
            String trimmed = ref.trim();
            if (trimmed.isEmpty()) {
                continue;
            }
            ResourceLocation location = new ResourceLocation(trimmed);
            int[] size = readPngSize(location);
            if (size != null) {
                images.add(new GuideImage(location, size[0], size[1]));
            }
        }
        return images.toArray(new GuideImage[0]);
    }

    private static String parseSpotlightItemId(String type, String detail) {
        if (!"modonomicon:spotlight".equals(type) || detail == null) {
            return "";
        }
        String prefix = "Item: ";
        if (!detail.startsWith(prefix)) {
            return "";
        }
        String itemId = detail.substring(prefix.length()).trim();
        int whitespace = itemId.indexOf(' ');
        if (whitespace >= 0) {
            itemId = itemId.substring(0, whitespace);
        }
        return itemId;
    }

    private static GuideReference parseReference(String type, String detail) {
        if (type == null || detail == null) {
            return null;
        }
        if ("occultism:ritual_recipe".equals(type)) {
            String id = parsePrefixedDetail(detail, "Recipe: ");
            return id.isEmpty() ? null : createRitualReference(id);
        }
        if ("occultism:spirit_fire_recipe".equals(type)) {
            String id = parsePrefixedDetail(detail, "Recipe: ");
            return id.isEmpty() ? null : createSpiritFireReference(id);
        }
        if ("modonomicon:smelting_recipe".equals(type)) {
            String id = parsePrefixedDetail(detail, "Recipe: ");
            return id.isEmpty() ? null : createSmeltingReference(id);
        }
        if ("modonomicon:crafting_recipe".equals(type)) {
            String id = parsePrefixedDetail(detail, "Recipe: ");
            return id.isEmpty() ? null : createCraftingReference(id);
        }
        if ("occultism:book_binding_recipe".equals(type)) {
            String id = parsePrefixedDetail(detail, "Recipe: ");
            return id.isEmpty() ? null : createBookBindingReference(id);
        }
        if ("occultism:spirit_trade_recipe".equals(type)) {
            String id = parsePrefixedDetail(detail, "Recipe: ");
            return id.isEmpty() ? null : createSpiritTradeReference(id);
        }
        if ("modonomicon:entity".equals(type)) {
            String id = parsePrefixedDetail(detail, "Entity: ");
            return id.isEmpty() ? null : createEntityReference(id);
        }
        if ("modonomicon:multiblock".equals(type)) {
            String id = parsePrefixedDetail(detail, "Multiblock: ");
            return id.isEmpty() ? null : createMultiblockReference(id);
        }
        return null;
    }

    private static GuideReference createRitualReference(String id) {
        String ritualId = removeCategoryPath(id, "ritual/");
        RitualDefinition ritual = RitualRegistry.getById(ritualId);
        List<GuideReferenceLine> lines = new ArrayList<>();
        List<ItemStack> stacks = new ArrayList<>();
        if (ritual == null) {
            lines.add(line("gui.occultism.dictionary.reference.unresolved", id));
            return reference("gui.occultism.dictionary.reference.ritual", id, lines, stacks, ritual);
        }

        lines.add(line("gui.occultism.dictionary.reference.ritual_type", ritual.getRitualType()));
        lines.add(line("gui.occultism.dictionary.reference.pentacle", formatPentacle(ritual.getPentacleId())));
        lines.add(line("gui.occultism.dictionary.reference.duration", String.valueOf(ritual.getDurationTicks())));
        lines.add(line("gui.occultism.dictionary.reference.activation",
                describeStack(ritual.getActivationIngredient().createDisplayStack())));
        String ingredients = describeIngredients(ritual.getAdditionalIngredients(), 5);
        if (!ingredients.isEmpty()) {
            lines.add(line("gui.occultism.dictionary.reference.ingredients", ingredients));
        }
        if (ritual.requiresSacrifice()) {
            lines.add(line("gui.occultism.dictionary.reference.sacrifice",
                    ritual.getSacrifice().getDisplayName()));
        }
        if (ritual.requiresItemUse()) {
            lines.add(line("gui.occultism.dictionary.reference.item_to_use",
                    describeStack(ritual.getItemToUse().createDisplayStack())));
        }
        if (ritual.summonsEntity()) {
            lines.add(line("gui.occultism.dictionary.reference.entity",
                    ritual.getEntityToSummon().getDisplayId()));
        } else {
            lines.add(line("gui.occultism.dictionary.reference.result",
                    describeStack(ritual.getResult().createStack())));
        }

        addStack(stacks, ritual.getActivationIngredient().createDisplayStack());
        for (RitualIngredient ingredient : ritual.getAdditionalIngredients()) {
            addStack(stacks, ingredient.createDisplayStack());
        }
        addStack(stacks, ritual.getResult().createStack());
        addStack(stacks, ritual.getRitualDummy().createStack());
        return reference("gui.occultism.dictionary.reference.ritual", id, lines, stacks, ritual);
    }

    private static GuideReference createSpiritFireReference(String id) {
        String recipeId = removeCategoryPath(id, "spirit_fire/");
        List<GuideReferenceLine> lines = new ArrayList<>();
        List<ItemStack> stacks = new ArrayList<>();
        SpiritFireRecipeRegistry.Recipe matched = null;
        for (SpiritFireRecipeRegistry.Recipe recipe : SpiritFireRecipeRegistry.getRecipes()) {
            ResourceLocation outputId = recipe.getOutput().getItem().getRegistryName();
            if (outputId != null && outputId.getPath().equals(recipeId)) {
                matched = recipe;
                break;
            }
        }
        if (matched == null) {
            lines.add(line("gui.occultism.dictionary.reference.unresolved", id));
            return reference("gui.occultism.dictionary.reference.spirit_fire", id, lines, stacks);
        }

        String inputs = describeStacks(matched.getInputs(), 5);
        if (!inputs.isEmpty()) {
            lines.add(line("gui.occultism.dictionary.reference.input", inputs));
        }
        lines.add(line("gui.occultism.dictionary.reference.result", describeStack(matched.getOutput())));
        for (ItemStack stack : matched.getInputs()) {
            addStack(stacks, stack);
        }
        addStack(stacks, matched.getOutput());
        return reference("gui.occultism.dictionary.reference.spirit_fire", id, lines, stacks);
    }

    private static GuideReference createSmeltingReference(String id) {
        String recipeId = removeCategoryPath(id, "smelting/");
        List<ItemStack> inputs = getSmeltingInputs(recipeId);
        ItemStack result = getSmeltingResult(recipeId);
        List<GuideReferenceLine> lines = new ArrayList<>();
        List<ItemStack> stacks = new ArrayList<>();
        if (inputs.isEmpty() || result.isEmpty()) {
            lines.add(line("gui.occultism.dictionary.reference.unresolved", id));
            return reference("gui.occultism.dictionary.reference.smelting", id, lines, stacks);
        }

        lines.add(line("gui.occultism.dictionary.reference.recipe_type", "minecraft:smelting"));
        lines.add(line("gui.occultism.dictionary.reference.input", describeStacks(inputs, 5)));
        lines.add(line("gui.occultism.dictionary.reference.result", describeStack(result)));
        for (ItemStack input : inputs) {
            addStack(stacks, input);
        }
        addStack(stacks, result);
        return reference("gui.occultism.dictionary.reference.smelting", id, lines, stacks);
    }

    private static List<ItemStack> getSmeltingInputs(String recipeId) {
        List<ItemStack> inputs = new ArrayList<>();
        ItemStack expected = getSmeltingResult(recipeId);
        if (expected.isEmpty()) {
            return inputs;
        }
        for (ItemStack candidate : getSmeltingInputCandidates(recipeId)) {
            ItemStack result = FurnaceRecipes.instance().getSmeltingResult(candidate);
            if (!result.isEmpty() && ItemStack.areItemsEqual(result, expected)) {
                addStack(inputs, candidate);
            }
        }
        return inputs;
    }

    private static List<ItemStack> getSmeltingInputCandidates(String recipeId) {
        List<ItemStack> inputs = new ArrayList<>();
        if ("silver_ingot".equals(recipeId)) {
            inputs.add(new ItemStack(ModBlocks.SILVER_ORE));
            inputs.add(new ItemStack(ModItems.RAW_SILVER));
            inputs.add(new ItemStack(ModItems.SILVER_DUST));
        } else if ("burnt_otherstone".equals(recipeId)) {
            inputs.add(new ItemStack(ModBlocks.POLISHED_OTHERSTONE));
        }
        return inputs;
    }

    private static ItemStack getSmeltingResult(String recipeId) {
        if ("silver_ingot".equals(recipeId)) {
            return new ItemStack(ModItems.SILVER_INGOT);
        }
        if ("burnt_otherstone".equals(recipeId)) {
            return new ItemStack(ModItems.BURNT_OTHERSTONE);
        }
        return ItemStack.EMPTY;
    }

    private static GuideReference createCraftingReference(String id) {
        return createJsonCraftingReference("gui.occultism.dictionary.reference.crafting", id);
    }

    private static GuideReference createBookBindingReference(String id) {
        return createJsonCraftingReference("gui.occultism.dictionary.reference.book_binding", id);
    }

    private static GuideReference createJsonCraftingReference(String labelKey, String id) {
        String recipeId = removeCategoryPath(id, "crafting/");
        JsonObject recipe = readJsonResource("assets/" + Tags.MOD_ID + "/recipes/" + recipeId + ".json");
        List<GuideReferenceLine> lines = new ArrayList<>();
        List<ItemStack> stacks = new ArrayList<>();
        if (recipe == null) {
            lines.add(line("gui.occultism.dictionary.reference.unresolved", id));
            return reference(labelKey, id, lines, stacks);
        }

        lines.add(line("gui.occultism.dictionary.reference.recipe_type", getString(recipe, "type")));
        ItemStack result = readStack(recipe.get("result"));
        if (!result.isEmpty()) {
            lines.add(line("gui.occultism.dictionary.reference.result", describeStack(result)));
            addStack(stacks, result);
        }
        List<ItemStack> ingredients = readCraftingInputs(recipe);
        String ingredientText = describeStacks(ingredients, 7);
        if (!ingredientText.isEmpty()) {
            lines.add(line("gui.occultism.dictionary.reference.ingredients", ingredientText));
        }
        for (ItemStack stack : ingredients) {
            addStack(stacks, stack);
        }
        return referenceWithCrafting(labelKey, id, lines, stacks, readCraftingPreview(recipe, result));
    }

    private static GuideReference createSpiritTradeReference(String id) {
        String recipeId = removeCategoryPath(id, "spirit_trade/");
        SpiritTradeRecipeRegistry.Recipe trade = SpiritTradeRecipeRegistry.find(recipeId);
        List<GuideReferenceLine> lines = new ArrayList<>();
        List<ItemStack> stacks = new ArrayList<>();
        if (trade == null) {
            lines.add(line("gui.occultism.dictionary.reference.unresolved", id));
            return reference("gui.occultism.dictionary.reference.spirit_trade", id, lines, stacks);
        }

        lines.add(line("gui.occultism.dictionary.reference.job", getJobDisplayName(trade.getJobType())));
        for (SpiritTradeIngredient ingredient : trade.getIngredients()) {
            List<ItemStack> inputs = ingredient.getDisplayInputs();
            lines.add(line("gui.occultism.dictionary.reference.input", describeStacks(inputs, 5)));
            if (!inputs.isEmpty()) {
                addStack(stacks, inputs.get(0));
            }
        }
        lines.add(line("gui.occultism.dictionary.reference.result", describeStack(trade.getOutput())));
        addStack(stacks, trade.getOutput());
        return reference("gui.occultism.dictionary.reference.spirit_trade", id, lines, stacks);
    }

    private static String getJobDisplayName(SpiritJobType jobType) {
        ResourceLocation id = jobType.getId();
        String key = "job." + id.getNamespace() + "." + id.getPath();
        return I18n.hasKey(key) ? I18n.format(key) : id.toString();
    }

    private static GuideReference createEntityReference(String id) {
        List<GuideReferenceLine> lines = new ArrayList<>();
        List<ItemStack> stacks = new ArrayList<>();
        lines.add(line("gui.occultism.dictionary.reference.entity", id));
        EntityReferenceMapping mapping = getEntityReferenceMapping(id);
        if (mapping != null) {
            lines.add(line("gui.occultism.dictionary.reference.local_substitute", mapping.localId));
            lines.add(line(mapping.noteKey));
        } else if (isRegisteredEntity(id)) {
            lines.add(line("gui.occultism.dictionary.reference.local_entity", id));
            lines.add(line("gui.occultism.dictionary.reference.entity_registered_note"));
        } else {
            lines.add(line("gui.occultism.dictionary.reference.entity_unresolved", id));
            lines.add(line("gui.occultism.dictionary.reference.entity_note"));
        }
        return reference("gui.occultism.dictionary.reference.entity_page", id, lines, stacks);
    }

    private static boolean isRegisteredEntity(String id) {
        try {
            return EntityList.isRegistered(new ResourceLocation(id));
        } catch (RuntimeException e) {
            return false;
        }
    }

    private static EntityReferenceMapping getEntityReferenceMapping(String id) {
        String localId = getLocalEntitySubstitute(id);
        if (localId.isEmpty()) {
            return null;
        }
        String noteKey = localId.startsWith(Tags.MOD_ID + ":")
                ? "gui.occultism.dictionary.reference.entity_local_substitute_note"
                : "gui.occultism.dictionary.reference.entity_vanilla_substitute_note";
        return new EntityReferenceMapping(localId, noteKey);
    }

    private static String getLocalEntitySubstitute(String id) {
        switch (id) {
            case "occultism:otherworld_bird":
            case "minecraft:allay":
                return "minecraft:parrot";
            case "occultism:iesnium_golem":
                return "occultism:iesnium_golem";
            case "occultism:possessed_bee":
            case "occultism:mercy_goat":
            case "occultism:demonic_husband":
            case "occultism:demonic_wife":
                return id;
            default:
                return "";
        }
    }

    private static GuideReference createMultiblockReference(String id) {
        String pentacleId = removeNamespace(id);
        List<GuideReferenceLine> lines = new ArrayList<>();
        List<ItemStack> stacks = new ArrayList<>();
        PentacleDefinition pentacle = findPentacle(pentacleId);
        if (pentacle == null) {
            lines.add(line("gui.occultism.dictionary.reference.multiblock", id));
            lines.add(line("gui.occultism.dictionary.reference.unresolved", id));
        } else {
            lines.add(line("gui.occultism.dictionary.reference.pentacle", pentacle.getDisplayName()));
            lines.add(line("gui.occultism.dictionary.reference.size",
                    pentacle.getWidth() + "x" + pentacle.getHeight()));
            lines.add(line("gui.occultism.dictionary.reference.center",
                    pentacle.getCenterX() + ", " + pentacle.getCenterZ()));
            lines.add(line("gui.occultism.dictionary.reference.multiblock_preview"));
        }
        return reference("gui.occultism.dictionary.reference.multiblock_page", id, lines, stacks, pentacle);
    }

    private static GuideReference reference(String labelKey, String id, List<GuideReferenceLine> lines,
            List<ItemStack> stacks) {
        return new GuideReference(labelKey, id, lines, stacks, null, null, null);
    }

    private static GuideReference reference(String labelKey, String id, List<GuideReferenceLine> lines,
            List<ItemStack> stacks, PentacleDefinition pentaclePreview) {
        return new GuideReference(labelKey, id, lines, stacks, pentaclePreview, null, null);
    }

    private static GuideReference referenceWithCrafting(String labelKey, String id, List<GuideReferenceLine> lines,
            List<ItemStack> stacks, CraftingGridPreview craftingPreview) {
        return new GuideReference(labelKey, id, lines, stacks, null, craftingPreview, null);
    }

    private static GuideReference reference(String labelKey, String id, List<GuideReferenceLine> lines,
            List<ItemStack> stacks, RitualDefinition ritualPreview) {
        return new GuideReference(labelKey, id, lines, stacks, null, null, ritualPreview);
    }

    private static GuideReferenceLine line(String labelKey) {
        return new GuideReferenceLine(labelKey);
    }

    private static GuideReferenceLine line(String labelKey, String value) {
        return new GuideReferenceLine(labelKey, value);
    }

    private static String parsePrefixedDetail(String detail, String prefix) {
        if (detail == null || !detail.startsWith(prefix)) {
            return "";
        }
        String id = detail.substring(prefix.length()).trim();
        int whitespace = id.indexOf(' ');
        if (whitespace >= 0) {
            id = id.substring(0, whitespace);
        }
        return id;
    }

    private static String removeCategoryPath(String id, String categoryPath) {
        String path = removeNamespace(id);
        return path.startsWith(categoryPath) ? path.substring(categoryPath.length()) : path;
    }

    private static String removeNamespace(String id) {
        int separator = id.indexOf(':');
        return separator >= 0 ? id.substring(separator + 1) : id;
    }

    private static String formatPentacle(String pentacleId) {
        PentacleDefinition pentacle = findPentacle(removeNamespace(pentacleId));
        return pentacle == null ? pentacleId : pentacle.getDisplayName() + " (" + pentacleId + ")";
    }

    private static PentacleDefinition findPentacle(String pentacleId) {
        for (PentacleDefinition pentacle : PentacleDetector.getDefinitions()) {
            if (pentacle.getId().equals(pentacleId)) {
                return pentacle;
            }
        }
        return null;
    }

    private static String describeIngredients(List<RitualIngredient> ingredients, int limit) {
        List<ItemStack> stacks = new ArrayList<>();
        for (RitualIngredient ingredient : ingredients) {
            ItemStack stack = ingredient.createDisplayStack();
            if (!stack.isEmpty()) {
                stacks.add(stack);
            } else if (ingredient.getOreName() != null) {
                stacks.add(ItemStack.EMPTY);
            }
        }
        return describeStacks(stacks, limit);
    }

    private static String describeStacks(List<ItemStack> stacks, int limit) {
        List<String> names = new ArrayList<>();
        int omitted = 0;
        for (ItemStack stack : stacks) {
            if (stack.isEmpty()) {
                omitted++;
                continue;
            }
            if (names.size() >= limit) {
                omitted++;
                continue;
            }
            names.add(describeStack(stack));
        }
        if (omitted > 0) {
            names.add(I18n.format("gui.occultism.dictionary.reference.more", omitted));
        }
        return String.join(", ", names);
    }

    private static String describeStack(ItemStack stack) {
        if (stack.isEmpty()) {
            return "";
        }
        String name = stack.getDisplayName();
        return stack.getCount() > 1 ? stack.getCount() + "x " + name : name;
    }

    private static void addStack(List<ItemStack> stacks, ItemStack stack) {
        if (stack.isEmpty()) {
            return;
        }
        for (ItemStack existing : stacks) {
            if (existing.getItem() == stack.getItem()
                    && existing.getMetadata() == stack.getMetadata()
                    && ItemStack.areItemStackTagsEqual(existing, stack)) {
                return;
            }
        }
        stacks.add(stack.copy());
    }

    private static JsonObject readJsonResource(String path) {
        try (InputStream stream = GuiDictionaryOfSpirits.class.getClassLoader().getResourceAsStream(path)) {
            if (stream == null) {
                return null;
            }
            JsonElement element = new JsonParser().parse(new InputStreamReader(stream, StandardCharsets.UTF_8));
            return element.isJsonObject() ? element.getAsJsonObject() : null;
        } catch (Exception e) {
            Occultism.LOGGER.error("Failed to read Dictionary of Spirits recipe reference " + path + ".", e);
            return null;
        }
    }

    private static List<ItemStack> readCraftingInputs(JsonObject recipe) {
        List<ItemStack> stacks = new ArrayList<>();
        if (recipe.has("ingredients") && recipe.get("ingredients").isJsonArray()) {
            for (JsonElement element : recipe.getAsJsonArray("ingredients")) {
                addStack(stacks, readStack(element));
            }
        }
        if (recipe.has("key") && recipe.get("key").isJsonObject()) {
            JsonObject key = recipe.getAsJsonObject("key");
            for (java.util.Map.Entry<String, JsonElement> entry : key.entrySet()) {
                addStack(stacks, readStack(entry.getValue()));
            }
        }
        return stacks;
    }

    private static CraftingGridPreview readCraftingPreview(JsonObject recipe, ItemStack result) {
        if (result.isEmpty()) {
            return null;
        }
        ItemStack[] slots = emptyCraftingGrid();
        boolean hasInput = false;
        if (recipe.has("pattern") && recipe.get("pattern").isJsonArray()
                && recipe.has("key") && recipe.get("key").isJsonObject()) {
            JsonArray pattern = recipe.getAsJsonArray("pattern");
            JsonObject key = recipe.getAsJsonObject("key");
            for (int row = 0; row < pattern.size() && row < CRAFTING_GRID_SIZE; row++) {
                String line = pattern.get(row).getAsString();
                for (int column = 0; column < line.length() && column < CRAFTING_GRID_SIZE; column++) {
                    char symbol = line.charAt(column);
                    if (symbol == ' ') {
                        continue;
                    }
                    JsonElement ingredient = key.get(String.valueOf(symbol));
                    ItemStack stack = readStack(ingredient);
                    slots[row * CRAFTING_GRID_SIZE + column] = stack;
                    hasInput |= !stack.isEmpty();
                }
            }
        } else if (recipe.has("ingredients") && recipe.get("ingredients").isJsonArray()) {
            JsonArray ingredients = recipe.getAsJsonArray("ingredients");
            int slot = 0;
            for (JsonElement ingredient : ingredients) {
                if (slot >= slots.length) {
                    break;
                }
                ItemStack stack = readStack(ingredient);
                slots[slot++] = stack;
                hasInput |= !stack.isEmpty();
            }
        }
        return hasInput ? new CraftingGridPreview(slots, result.copy()) : null;
    }

    private static ItemStack[] emptyCraftingGrid() {
        ItemStack[] slots = new ItemStack[CRAFTING_GRID_SIZE * CRAFTING_GRID_SIZE];
        for (int i = 0; i < slots.length; i++) {
            slots[i] = ItemStack.EMPTY;
        }
        return slots;
    }

    private static ItemStack readStack(JsonElement element) {
        if (element == null || element.isJsonNull()) {
            return ItemStack.EMPTY;
        }
        if (element.isJsonArray()) {
            for (JsonElement alternative : element.getAsJsonArray()) {
                ItemStack stack = readStack(alternative);
                if (!stack.isEmpty()) {
                    return stack;
                }
            }
            return ItemStack.EMPTY;
        }
        if (element.isJsonPrimitive()) {
            return readStack(element.getAsString(), 1, -1);
        }
        JsonObject object = element.getAsJsonObject();
        if (object.has("item")) {
            String itemId = object.get("item").getAsString();
            int count = getInt(object, "count", 1);
            int metadata = getInt(object, "data", getInt(object, "metadata", getInt(object, "meta", -1)));
            return readStack(itemId, count, metadata);
        }
        if (object.has("id")) {
            String itemId = object.get("id").getAsString();
            int count = getInt(object, "count", 1);
            int metadata = getInt(object, "data", getInt(object, "metadata", getInt(object, "meta", -1)));
            return readStack(itemId, count, metadata);
        }
        String oreName = object.has("ore") ? object.get("ore").getAsString()
                : object.has("tag") ? object.get("tag").getAsString() : "";
        if (!oreName.isEmpty()) {
            List<ItemStack> ores = OreDictionary.getOres(toOreName(oreName));
            if (!ores.isEmpty()) {
                ItemStack stack = ores.get(0).copy();
                stack.setCount(getInt(object, "count", 1));
                if (stack.getMetadata() == OreDictionary.WILDCARD_VALUE) {
                    stack.setItemDamage(0);
                }
                return stack;
            }
        }
        return ItemStack.EMPTY;
    }

    private static ItemStack readStack(String itemId, int count, int metadata) {
        try {
            Item item = ForgeRegistries.ITEMS.getValue(new ResourceLocation(itemId));
            if (item == null || item == Items.AIR) {
                return ItemStack.EMPTY;
            }
            return metadata < 0 ? new ItemStack(item, count) : new ItemStack(item, count, metadata);
        } catch (RuntimeException e) {
            return ItemStack.EMPTY;
        }
    }

    private static int getInt(JsonObject object, String key, int fallback) {
        return object.has(key) && !object.get(key).isJsonNull() ? object.get(key).getAsInt() : fallback;
    }

    private static String toOreName(String name) {
        String clean = name.startsWith("#") ? name.substring(1) : name;
        int separator = clean.indexOf(':');
        if (separator >= 0) {
            clean = clean.substring(separator + 1);
        }
        String[] parts = clean.split("/");
        if (parts.length == 1) {
            return parts[0];
        }
        String kind = parts[parts.length - 2];
        String material = parts[parts.length - 1];
        if ("storage_blocks".equals(kind)) {
            kind = "block";
        } else if (kind.endsWith("s") && kind.length() > 1) {
            kind = kind.substring(0, kind.length() - 1);
        }
        return kind + Character.toUpperCase(material.charAt(0)) + material.substring(1);
    }

    private static ItemStack parseSpotlightStack(String itemId) {
        if (itemId == null || itemId.isEmpty() || itemId.startsWith("#")) {
            return ItemStack.EMPTY;
        }

        ItemStack legacyStack = getLegacySpotlightStack(itemId);
        if (!legacyStack.isEmpty()) {
            return legacyStack;
        }

        try {
            Item item = ForgeRegistries.ITEMS.getValue(new ResourceLocation(itemId));
            if (item == null || item == Items.AIR) {
                return ItemStack.EMPTY;
            }
            return new ItemStack(item);
        } catch (RuntimeException e) {
            return ItemStack.EMPTY;
        }
    }

    private static ItemStack getLegacySpotlightStack(String itemId) {
        if ("minecraft:skeleton_skull".equals(itemId)) {
            return new ItemStack(Items.SKULL, 1, 0);
        }
        if ("minecraft:wither_skeleton_skull".equals(itemId)) {
            return new ItemStack(Items.SKULL, 1, 1);
        }
        if ("minecraft:nautilus_shell".equals(itemId)) {
            Item nautilusShell = ForgeRegistries.ITEMS.getValue(new ResourceLocation(Tags.MOD_ID, "nautilus_shell"));
            return nautilusShell == null || nautilusShell == Items.AIR ? ItemStack.EMPTY : new ItemStack(nautilusShell);
        }
        return ItemStack.EMPTY;
    }

    private static int[] readPngSize(ResourceLocation location) {
        String path = "assets/" + location.getNamespace() + "/" + location.getPath();
        try (InputStream stream = GuiDictionaryOfSpirits.class.getClassLoader().getResourceAsStream(path)) {
            if (stream == null) {
                return null;
            }
            byte[] header = new byte[24];
            int read = stream.read(header);
            if (read < header.length || !isPngHeader(header)) {
                return null;
            }
            int width = readBigEndianInt(header, 16);
            int height = readBigEndianInt(header, 20);
            if (width <= 0 || height <= 0) {
                return null;
            }
            return new int[] { width, height };
        } catch (IOException e) {
            Occultism.LOGGER.error("Failed to read Dictionary of Spirits image size for " + location + ".", e);
            return null;
        }
    }

    private static boolean isPngHeader(byte[] header) {
        return header[0] == (byte) 0x89 && header[1] == 0x50 && header[2] == 0x4E && header[3] == 0x47
                && header[4] == 0x0D && header[5] == 0x0A && header[6] == 0x1A && header[7] == 0x0A;
    }

    private static int readBigEndianInt(byte[] bytes, int offset) {
        return (bytes[offset] & 0xFF) << 24
                | (bytes[offset + 1] & 0xFF) << 16
                | (bytes[offset + 2] & 0xFF) << 8
                | bytes[offset + 3] & 0xFF;
    }
}
