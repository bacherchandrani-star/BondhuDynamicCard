
package com.bondhubazar.dynamiccard.bondhudynamiccard;

import android.graphics.Color;
import android.graphics.Typeface;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.drawable.GradientDrawable;
import android.view.Gravity;
import android.view.View;
import android.view.ViewGroup;
import android.widget.FrameLayout;
import android.widget.LinearLayout;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Button;

import com.google.appinventor.components.annotations.*;
import com.google.appinventor.components.common.ComponentCategory;
import com.google.appinventor.components.runtime.*;

import java.net.HttpURLConnection;
import java.net.URL;
import java.util.HashMap;

@DesignerComponent(
    version = 2,
    description = "Bondhu Bazar Two Column Product Cards with Cart and Favorites",
    category = ComponentCategory.EXTENSION,
    nonVisible = true,
    iconName = "images/extension.png"
)
@SimpleObject(external = true)
public class BondhuDynamicCard extends AndroidNonvisibleComponent {

    private final Form form;

    private final HashMap<String, View> cards = new HashMap<>();
    private final HashMap<String, TextView> titles = new HashMap<>();
    private final HashMap<String, TextView> prices = new HashMap<>();
    private final HashMap<String, TextView> packs = new HashMap<>();
    private final HashMap<String, TextView> favorites = new HashMap<>();
    private final HashMap<String, TextView> quantityLabels = new HashMap<>();
    private final HashMap<String, View> addButtons = new HashMap<>();
    private final HashMap<String, View> quantityRows = new HashMap<>();
    private final HashMap<String, Integer> quantities = new HashMap<>();
    private final HashMap<String, Boolean> favoriteStates = new HashMap<>();

    private ViewGroup gridParent;
    private LinearLayout currentRow;
    private int cardsInRow = 0;

    public BondhuDynamicCard(ComponentContainer container) {
        super(container.$form());
        form = container.$form();
    }

    private int dp(float value) {
        return (int) (value * form.getResources()
            .getDisplayMetrics().density + 0.5f);
    }

    private GradientDrawable rounded(int color, int radius) {
        GradientDrawable bg = new GradientDrawable();
        bg.setColor(color);
        bg.setCornerRadius(dp(radius));
        return bg;
    }

    private TextView makeText(String text, int size, int color) {
        TextView v = new TextView(form);
        v.setText(text);
        v.setTextSize(size);
        v.setTextColor(color);
        return v;
    }

    @SimpleFunction(description = "Create a product card in a two-column grid.")
    public void CreateCard(
        HVArrangement parent,
        String id,
        String title,
        String pack,
        String price
    ) {
        View parentView = parent.getView();

        if (!(parentView instanceof ViewGroup)) {
            return;
        }

        ViewGroup container = (ViewGroup) parentView;

        if (gridParent != container) {
            gridParent = container;
            currentRow = null;
            cardsInRow = 0;
        }

        if (cards.containsKey(id)) {
            RemoveCard(id);
        }

        if (currentRow == null || cardsInRow >= 2) {
            currentRow = new LinearLayout(form);
            currentRow.setOrientation(LinearLayout.HORIZONTAL);
            currentRow.setGravity(Gravity.TOP);

            LinearLayout.LayoutParams rowParams =
                new LinearLayout.LayoutParams(
                    ViewGroup.LayoutParams.MATCH_PARENT,
                    ViewGroup.LayoutParams.WRAP_CONTENT
                );

            rowParams.setMargins(dp(6), dp(6), dp(6), dp(6));
            container.addView(currentRow, rowParams);
            cardsInRow = 0;
        }

        LinearLayout card = new LinearLayout(form);
        card.setOrientation(LinearLayout.VERTICAL);
        card.setPadding(dp(7), dp(7), dp(7), dp(8));
        card.setBackground(rounded(Color.WHITE, 16));
        card.setElevation(dp(4));
        card.setClipToOutline(true);

        FrameLayout imageFrame = new FrameLayout(form);

        ImageView image = new ImageView(form);
        image.setScaleType(ImageView.ScaleType.CENTER_CROP);

        FrameLayout.LayoutParams imageParams =
            new FrameLayout.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT,
                dp(145)
            );

        imageFrame.addView(image, imageParams);

        TextView heart = makeText("♡", 32, Color.RED);
        heart.setGravity(Gravity.CENTER);
        heart.setBackground(rounded(Color.WHITE, 30));
        heart.setElevation(dp(2));

        FrameLayout.LayoutParams heartParams =
            new FrameLayout.LayoutParams(
                dp(42), dp(42),
                Gravity.RIGHT | Gravity.TOP
            );

        imageFrame.addView(heart, heartParams);
        card.addView(imageFrame);

        TextView name = makeText(title, 19, Color.BLACK);
        name.setTypeface(null, Typeface.BOLD);
        name.setPadding(dp(2), dp(8), dp(2), dp(1));
        card.addView(name);

        TextView packText = makeText(pack, 14, Color.GRAY);
        packText.setPadding(dp(2), dp(2), dp(2), dp(2));
        card.addView(packText);

        TextView priceText = makeText(
            price, 20, Color.rgb(0, 125, 35)
        );
        priceText.setTypeface(null, Typeface.BOLD);
        priceText.setPadding(dp(2), dp(3), dp(2), dp(5));
        card.addView(priceText);

        Button addButton = new Button(form);
        addButton.setText("ADD TO CART");
        addButton.setTextSize(14);
        addButton.setTextColor(Color.WHITE);
        addButton.setAllCaps(false);
        addButton.setTypeface(null, Typeface.BOLD);
        addButton.setBackground(
            rounded(Color.rgb(0, 165, 225), 10)
        );

        LinearLayout.LayoutParams buttonParams =
            new LinearLayout.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT,
                dp(42)
            );

        card.addView(addButton, buttonParams);

        LinearLayout qtyRow = new LinearLayout(form);
        qtyRow.setOrientation(LinearLayout.HORIZONTAL);
        qtyRow.setGravity(Gravity.CENTER);
        qtyRow.setVisibility(View.GONE);

        Button minus = new Button(form);
        minus.setText("−");
        minus.setTextSize(22);
        minus.setTextColor(Color.WHITE);
        minus.setBackground(rounded(Color.RED, 30));

        TextView qtyText = makeText("0", 20, Color.BLACK);
        qtyText.setGravity(Gravity.CENTER);
        qtyText.setBackground(
            rounded(Color.rgb(225, 250, 230), 8)
        );

        Button plus = new Button(form);
        plus.setText("+");
        plus.setTextSize(22);
        plus.setTextColor(Color.WHITE);
        plus.setBackground(
            rounded(Color.rgb(0, 155, 35), 30)
        );

        LinearLayout.LayoutParams controlParams =
            new LinearLayout.LayoutParams(dp(42), dp(42));

        LinearLayout.LayoutParams numberParams =
            new LinearLayout.LayoutParams(
                0, dp(42), 1
            );

        numberParams.setMargins(dp(5), 0, dp(5), 0);

        qtyRow.addView(minus, controlParams);
        qtyRow.addView(qtyText, numberParams);
        qtyRow.addView(plus, controlParams);

        card.addView(qtyRow);

        cards.put(id, card);
        titles.put(id, name);
        prices.put(id, priceText);
        packs.put(id, packText);
        favorites.put(id, heart);
        quantityLabels.put(id, qtyText);
        addButtons.put(id, addButton);
        quantityRows.put(id, qtyRow);
        quantities.put(id, 0);
        favoriteStates.put(id, false);

        heart.setOnClickListener(v -> {
            boolean selected = !favoriteStates.get(id);
            favoriteStates.put(id, selected);

            heart.setText(selected ? "♥" : "♡");
            heart.setTextColor(Color.RED);

            FavoriteChanged(id, selected);
        });

        addButton.setOnClickListener(v -> {
            quantities.put(id, 1);
            qtyText.setText("1");

            addButton.setVisibility(View.GONE);
            qtyRow.setVisibility(View.VISIBLE);

            AddToCartClicked(id);
            QuantityChanged(id, 1);
        });

        plus.setOnClickListener(v -> {
            int qty = quantities.get(id) + 1;
            quantities.put(id, qty);
            qtyText.setText(String.valueOf(qty));

            QuantityChanged(id, qty);
        });

        minus.setOnClickListener(v -> {
            int qty = quantities.get(id) - 1;

            if (qty <= 0) {
                qty = 0;
                qtyRow.setVisibility(View.GONE);
                addButton.setVisibility(View.VISIBLE);
            }

            quantities.put(id, qty);
            qtyText.setText(String.valueOf(qty));

            QuantityChanged(id, qty);
        });

        card.setOnClickListener(v -> {
            CardClicked(id);
        });

        LinearLayout.LayoutParams cardParams =
            new LinearLayout.LayoutParams(
                0,
                ViewGroup.LayoutParams.WRAP_CONTENT,
                1.0f
            );

        cardParams.setMargins(dp(4), dp(3), dp(4), dp(3));
        currentRow.addView(card, cardParams);

        cardsInRow++;

        CardCreated(id);
    }

    @SimpleFunction(description = "Set product image using URL.")
    public void SetImage(String id, String imageUrl) {
        View card = cards.get(id);
        if (card == null) return;

        ImageView image = (ImageView)
            ((FrameLayout) ((LinearLayout) card).getChildAt(0))
            .getChildAt(0);

        new Thread(() -> {
            HttpURLConnection connection = null;

            try {
                URL url = new URL(imageUrl);
                connection = (HttpURLConnection) url.openConnection();
                connection.setConnectTimeout(10000);
                connection.setReadTimeout(10000);
                connection.setDoInput(true);
                connection.connect();

                Bitmap bitmap = BitmapFactory.decodeStream(
                    connection.getInputStream()
                );

                if (bitmap == null) {
                    throw new Exception("Image could not be decoded");
                }

                form.runOnUiThread(
                    () -> image.setImageBitmap(bitmap)
                );

            } catch (Exception e) {
                form.runOnUiThread(
                    () -> ImageError(id, e.getMessage())
                );

            } finally {
                if (connection != null) {
                    connection.disconnect();
                }
            }
        }).start();
    }

    @SimpleFunction(description = "Change product name.")
    public void SetTitle(String id, String title) {
        if (titles.containsKey(id)) {
            titles.get(id).setText(title);
        }
    }

    @SimpleFunction(description = "Change product price.")
    public void SetPrice(String id, String price) {
        if (prices.containsKey(id)) {
            prices.get(id).setText(price);
        }
    }

    @SimpleFunction(description = "Change product pack size.")
    public void SetPack(String id, String pack) {
        if (packs.containsKey(id)) {
            packs.get(id).setText(pack);
        }
    }

    @SimpleFunction(description = "Get current product quantity.")
    public int GetQuantity(String id) {
        Integer qty = quantities.get(id);
        return qty == null ? 0 : qty;
    }

    @SimpleFunction(description = "Check whether product is favorite.")
    public boolean IsFavorite(String id) {
        Boolean value = favoriteStates.get(id);
        return value != null && value;
    }

    @SimpleFunction(description = "Remove a product card.")
    public void RemoveCard(String id) {
        View card = cards.get(id);

        if (card != null && card.getParent() instanceof ViewGroup) {
            ((ViewGroup) card.getParent()).removeView(card);
        }

        cards.remove(id);
        titles.remove(id);
        prices.remove(id);
        packs.remove(id);
        favorites.remove(id);
        quantityLabels.remove(id);
        addButtons.remove(id);
        quantityRows.remove(id);
        quantities.remove(id);
        favoriteStates.remove(id);
    }

    @SimpleFunction(description = "Remove all product cards.")
    public void ClearCards() {
        if (gridParent != null) {
            gridParent.removeAllViews();
        }

        cards.clear();
        titles.clear();
        prices.clear();
        packs.clear();
        favorites.clear();
        quantityLabels.clear();
        addButtons.clear();
        quantityRows.clear();
        quantities.clear();
        favoriteStates.clear();

        currentRow = null;
        cardsInRow = 0;
    }

    @SimpleEvent(description = "Fires when a product card is created.")
    public void CardCreated(String id) {
        EventDispatcher.dispatchEvent(this, "CardCreated", id);
    }

    @SimpleEvent(description = "Fires when a product card is clicked.")
    public void CardClicked(String id) {
        EventDispatcher.dispatchEvent(this, "CardClicked", id);
    }

    @SimpleEvent(description = "Fires when Add to Cart is clicked.")
    public void AddToCartClicked(String id) {
        EventDispatcher.dispatchEvent(this, "AddToCartClicked", id);
    }

    @SimpleEvent(description = "Fires when quantity changes.")
    public void QuantityChanged(String id, int quantity) {
        EventDispatcher.dispatchEvent(
            this, "QuantityChanged", id, quantity
        );
    }

    @SimpleEvent(description = "Fires when favorite status changes.")
    public void FavoriteChanged(String id, boolean isFavorite) {
        EventDispatcher.dispatchEvent(
            this, "FavoriteChanged", id, isFavorite
        );
    }

    @SimpleEvent(description = "Fires if image loading fails.")
    public void ImageError(String id, String message) {
        EventDispatcher.dispatchEvent(
            this, "ImageError", id, message
        );
    }
}
