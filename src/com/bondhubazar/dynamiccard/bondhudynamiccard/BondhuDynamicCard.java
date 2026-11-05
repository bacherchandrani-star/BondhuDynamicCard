
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

import com.google.appinventor.components.annotations.Extension;
import com.google.appinventor.components.annotations.SimpleFunction;
import com.google.appinventor.components.annotations.SimpleEvent;
import com.google.appinventor.components.runtime.AndroidNonvisibleComponent;
import com.google.appinventor.components.runtime.ComponentContainer;
import com.google.appinventor.components.runtime.HVArrangement;
import com.google.appinventor.components.runtime.Form;
import com.google.appinventor.components.runtime.EventDispatcher;

import java.net.HttpURLConnection;
import java.net.URL;
import java.util.HashMap;

@Extension(
    description = "Bondhu Bazar Horizontal Product Cards with Cart and Favorites.",
    icon = "icon.png"
)
public class BondhuDynamicCard extends AndroidNonvisibleComponent {

    private final Form form;

    private final HashMap<String, View> cards = new HashMap<>();
    private final HashMap<String, TextView> titles = new HashMap<>();
    private final HashMap<String, TextView> prices = new HashMap<>();
    private final HashMap<String, TextView> packs = new HashMap<>();
    private final HashMap<String, TextView> hearts = new HashMap<>();
    private final HashMap<String, ImageView> images = new HashMap<>();
    private final HashMap<String, TextView> quantityLabels = new HashMap<>();
    private final HashMap<String, Button> addButtons = new HashMap<>();
    private final HashMap<String, LinearLayout> quantityRows = new HashMap<>();

    private final HashMap<String, Integer> quantities = new HashMap<>();
    private final HashMap<String, Boolean> favoriteStates = new HashMap<>();

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
        TextView view = new TextView(form);
        view.setText(text);
        view.setTextSize(size);
        view.setTextColor(color);
        return view;
    }

    @SimpleFunction(description = "Create a horizontal product card.")
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

        if (cards.containsKey(id)) {
            RemoveCard(id);
        }

        // Main card
        LinearLayout card = new LinearLayout(form);
        card.setOrientation(LinearLayout.VERTICAL);
        card.setPadding(dp(10), dp(10), dp(10), dp(10));
        card.setBackground(rounded(Color.WHITE, 18));
        card.setElevation(dp(4));

        // Top horizontal section
        LinearLayout top = new LinearLayout(form);
        top.setOrientation(LinearLayout.HORIZONTAL);
        top.setGravity(Gravity.CENTER_VERTICAL);

        // Product image
        ImageView image = new ImageView(form);
        image.setScaleType(ImageView.ScaleType.CENTER_CROP);
        image.setBackground(rounded(Color.rgb(248, 248, 248), 14));
        image.setClipToOutline(true);

        LinearLayout.LayoutParams imageParams =
            new LinearLayout.LayoutParams(dp(112), dp(112));

        top.addView(image, imageParams);

        // Right side product details
        LinearLayout details = new LinearLayout(form);
        details.setOrientation(LinearLayout.VERTICAL);
        details.setGravity(Gravity.CENTER_VERTICAL);
        details.setPadding(dp(12), 0, 0, 0);

        LinearLayout.LayoutParams detailsParams =
            new LinearLayout.LayoutParams(
                0,
                ViewGroup.LayoutParams.WRAP_CONTENT,
                1
            );

        top.addView(details, detailsParams);

        // Name and favorite row
        LinearLayout nameRow = new LinearLayout(form);
        nameRow.setOrientation(LinearLayout.HORIZONTAL);
        nameRow.setGravity(Gravity.CENTER_VERTICAL);

        TextView name = makeText(title, 20, Color.BLACK);
        name.setTypeface(null, Typeface.BOLD);
        name.setMaxLines(2);

        LinearLayout.LayoutParams nameParams =
            new LinearLayout.LayoutParams(
                0,
                ViewGroup.LayoutParams.WRAP_CONTENT,
                1
            );

        nameRow.addView(name, nameParams);

        TextView heart = makeText("♡", 30, Color.RED);
        heart.setGravity(Gravity.CENTER);

        nameRow.addView(
            heart,
            new LinearLayout.LayoutParams(dp(38), dp(42))
        );

        details.addView(nameRow);

        // Price
        TextView priceText = makeText(
            price, 24, Color.rgb(0, 120, 45)
        );

        priceText.setTypeface(null, Typeface.BOLD);
        priceText.setPadding(0, dp(5), 0, dp(3));

        details.addView(priceText);

        // Pack size
        TextView packText = makeText(
            pack, 14, Color.rgb(35, 130, 75)
        );

        packText.setPadding(dp(8), dp(4), dp(8), dp(4));
        packText.setBackground(
            rounded(Color.rgb(225, 245, 232), 20)
        );

        details.addView(packText);

        card.addView(top);

        // Add to cart button
        Button addButton = new Button(form);
        addButton.setText("🛒  Add to Cart");
        addButton.setTextSize(16);
        addButton.setTextColor(Color.WHITE);
        addButton.setAllCaps(false);
        addButton.setTypeface(null, Typeface.BOLD);
        addButton.setBackground(
            rounded(Color.rgb(20, 145, 55), 30)
        );

        LinearLayout.LayoutParams addParams =
            new LinearLayout.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT,
                dp(50)
            );

        addParams.setMargins(0, dp(12), 0, 0);
        card.addView(addButton, addParams);

        // Quantity control
        LinearLayout qtyRow = new LinearLayout(form);
        qtyRow.setOrientation(LinearLayout.HORIZONTAL);
        qtyRow.setGravity(Gravity.CENTER);

        qtyRow.setVisibility(View.GONE);

        Button minus = new Button(form);
        minus.setText("−");
        minus.setTextSize(24);
        minus.setTextColor(Color.WHITE);
        minus.setBackground(rounded(Color.RED, 30));

        TextView qtyText = makeText("1", 21, Color.BLACK);
        qtyText.setGravity(Gravity.CENTER);
        qtyText.setBackground(
            rounded(Color.rgb(225, 250, 230), 12)
        );

        Button plus = new Button(form);
        plus.setText("+");
        plus.setTextSize(24);
        plus.setTextColor(Color.WHITE);
        plus.setBackground(
            rounded(Color.rgb(0, 150, 45), 30)
        );

        LinearLayout.LayoutParams controlParams =
            new LinearLayout.LayoutParams(
                0, dp(48), 1
            );

        LinearLayout.LayoutParams numberParams =
            new LinearLayout.LayoutParams(
                0, dp(48), 2
            );

        numberParams.setMargins(dp(12), 0, dp(12), 0);

        qtyRow.addView(minus, controlParams);
        qtyRow.addView(qtyText, numberParams);
        qtyRow.addView(plus, controlParams);

        LinearLayout.LayoutParams qtyParams =
            new LinearLayout.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT,
                dp(50)
            );

        qtyParams.setMargins(0, dp(12), 0, 0);

        card.addView(qtyRow, qtyParams);

        // Save references
        cards.put(id, card);
        titles.put(id, name);
        prices.put(id, priceText);
        packs.put(id, packText);
        hearts.put(id, heart);
        images.put(id, image);
        quantityLabels.put(id, qtyText);
        addButtons.put(id, addButton);
        quantityRows.put(id, qtyRow);

        quantities.put(id, 0);
        favoriteStates.put(id, false);

        // Favorite click
        heart.setOnClickListener(v -> {
            boolean selected = !favoriteStates.get(id);

            favoriteStates.put(id, selected);
            heart.setText(selected ? "♥" : "♡");

            FavoriteChanged(id, selected);
        });

        // Add to cart
        addButton.setOnClickListener(v -> {
            quantities.put(id, 1);
            qtyText.setText("1");

            addButton.setVisibility(View.GONE);
            qtyRow.setVisibility(View.VISIBLE);

            AddToCartClicked(id);
            QuantityChanged(id, 1);
        });

        // Plus
        plus.setOnClickListener(v -> {
            int qty = quantities.get(id) + 1;

            quantities.put(id, qty);
            qtyText.setText(String.valueOf(qty));

            QuantityChanged(id, qty);
        });

        // Minus
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

        // Add card to parent
        LinearLayout.LayoutParams cardParams =
            new LinearLayout.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT,
                ViewGroup.LayoutParams.WRAP_CONTENT
            );

        cardParams.setMargins(dp(5), dp(7), dp(5), dp(7));

        container.addView(card, cardParams);

        CardCreated(id);
    }

    @SimpleFunction(description = "Set product image using URL.")
    public void SetImage(String id, String imageUrl) {
        ImageView image = images.get(id);

        if (image == null) {
            return;
        }

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
                    throw new Exception("Invalid image");
                }

                form.runOnUiThread(() -> {
                    image.setImageBitmap(bitmap);
                });

            } catch (Exception e) {
                form.runOnUiThread(() -> {
                    ImageError(id, e.getMessage());
                });

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
        hearts.remove(id);
        images.remove(id);
        quantityLabels.remove(id);
        addButtons.remove(id);
        quantityRows.remove(id);
        quantities.remove(id);
        favoriteStates.remove(id);
    }

    @SimpleFunction(description = "Remove all product cards.")
    public void ClearCards(HVArrangement parent) {
        View view = parent.getView();

        if (view instanceof ViewGroup) {
            ((ViewGroup) view).removeAllViews();
        }

        cards.clear();
        titles.clear();
        prices.clear();
        packs.clear();
        hearts.clear();
        images.clear();
        quantityLabels.clear();
        addButtons.clear();
        quantityRows.clear();
        quantities.clear();
        favoriteStates.clear();
    }

    @SimpleEvent(description = "Fires when a product card is created.")
    public void CardCreated(String id) {
        EventDispatcher.dispatchEvent(this, "CardCreated", id);
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

    @SimpleEvent(description = "Fires when favorite changes.")
    public void FavoriteChanged(String id, boolean isFavorite) {
        EventDispatcher.dispatchEvent(
            this, "FavoriteChanged", id, isFavorite
        );
    }

    @SimpleEvent(description = "Fires when image loading fails.")
    public void ImageError(String id, String message) {
        EventDispatcher.dispatchEvent(
            this, "ImageError", id, message
        );
    }
}
