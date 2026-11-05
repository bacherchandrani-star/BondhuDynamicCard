
package com.bondhubazar.dynamiccard.bondhudynamiccard;

import android.graphics.Color;
import android.graphics.Typeface;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.drawable.GradientDrawable;
import android.view.Gravity;
import android.view.View;
import android.view.ViewGroup;
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
import java.io.InputStream;
import java.util.HashMap;
import java.util.ArrayList;
import java.util.Locale;

@Extension(
    description = "Bondhu Bazar Dynamic Product Cards with Cart, Total Price and Favorites.",
    icon = "icon.png"
)
public class BondhuDynamicCard extends AndroidNonvisibleComponent {

    private final Form form;

    private final HashMap<String, View> cards = new HashMap<>();
    private final HashMap<String, ViewGroup> cardParents = new HashMap<>();
    private final HashMap<String, TextView> titles = new HashMap<>();
    private final HashMap<String, TextView> prices = new HashMap<>();
    private final HashMap<String, TextView> packs = new HashMap<>();
    private final HashMap<String, TextView> hearts = new HashMap<>();
    private final HashMap<String, ImageView> images = new HashMap<>();
    private final HashMap<String, TextView> quantitiesText = new HashMap<>();
    private final HashMap<String, TextView> totals = new HashMap<>();
    private final HashMap<String, Button> addButtons = new HashMap<>();
    private final HashMap<String, LinearLayout> quantityRows = new HashMap<>();

    private final HashMap<String, Integer> quantities = new HashMap<>();
    private final HashMap<String, Double> unitPrices = new HashMap<>();
    private final HashMap<String, Boolean> favorites = new HashMap<>();
    private final HashMap<String, Integer> imageRequests = new HashMap<>();

    private final int GREEN = Color.rgb(20, 83, 45);
    private final int LIGHT_GREEN = Color.rgb(232, 245, 233);

    public BondhuDynamicCard(ComponentContainer container) {
        super(container.$form());
        form = container.$form();
    }

    private int dp(float value) {
        return (int) (value * form.getResources()
            .getDisplayMetrics().density + 0.5f);
    }

    private GradientDrawable background(int color, int radius) {
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

    private double parsePrice(String value) {
        if (value == null) return 0;

        String cleaned = value.replaceAll("[^0-9.]", "");

        try {
            return Double.parseDouble(cleaned);
        } catch (Exception e) {
            return 0;
        }
    }

    private double getTotal(String id) {
        Integer qty = quantities.get(id);
        Double price = unitPrices.get(id);

        if (qty == null || price == null) return 0;

        return qty * price;
    }

    private void updateTotal(String id) {
        TextView total = totals.get(id);
        Integer qty = quantities.get(id);

        if (total == null || qty == null) return;

        double amount = getTotal(id);

        total.setText(
            "Total Price: ₹" +
            String.format(Locale.US, "%.2f", amount)
        );

        total.setVisibility(
            qty > 0 ? View.VISIBLE : View.GONE
        );
    }

    @SimpleFunction(description = "Create a vertical product card.")
    public void CreateCard(
        HVArrangement parent,
        String id,
        String title,
        String pack,
        String price
    ) {
        if (parent == null || id == null || id.trim().isEmpty()) return;

        View parentView = parent.getView();

        if (!(parentView instanceof ViewGroup)) return;

        ViewGroup container = (ViewGroup) parentView;

        if (container instanceof LinearLayout) {
            ((LinearLayout) container)
                .setOrientation(LinearLayout.VERTICAL);
        }

        if (cards.containsKey(id)) {
            RemoveCard(id);
        }

        // Main Card
        LinearLayout card = new LinearLayout(form);
        card.setOrientation(LinearLayout.VERTICAL);
        card.setPadding(dp(12), dp(12), dp(12), dp(12));
        card.setBackground(background(Color.WHITE, 18));
        card.setElevation(dp(4));

        // Top Section
        LinearLayout top = new LinearLayout(form);
        top.setOrientation(LinearLayout.HORIZONTAL);
        top.setGravity(Gravity.CENTER_VERTICAL);

        // Product Image
        ImageView image = new ImageView(form);
        image.setScaleType(ImageView.ScaleType.CENTER_CROP);
        image.setBackground(background(
            Color.rgb(245, 248, 245), 14
        ));
        image.setClipToOutline(true);

        top.addView(image,
            new LinearLayout.LayoutParams(dp(112), dp(112))
        );

        // Product Details
        LinearLayout details = new LinearLayout(form);
        details.setOrientation(LinearLayout.VERTICAL);
        details.setGravity(Gravity.CENTER_VERTICAL);
        details.setPadding(dp(12), 0, 0, 0);

        top.addView(details,
            new LinearLayout.LayoutParams(
                0,
                ViewGroup.LayoutParams.WRAP_CONTENT,
                1
            )
        );

        // Name and Heart
        LinearLayout nameRow = new LinearLayout(form);
        nameRow.setOrientation(LinearLayout.HORIZONTAL);
        nameRow.setGravity(Gravity.CENTER_VERTICAL);

        TextView name = makeText(title, 19, Color.BLACK);
        name.setTypeface(null, Typeface.BOLD);
        name.setMaxLines(2);

        nameRow.addView(name,
            new LinearLayout.LayoutParams(
                0,
                ViewGroup.LayoutParams.WRAP_CONTENT,
                1
            )
        );

        TextView heart = makeText("♡", 27, GREEN);
        heart.setGravity(Gravity.CENTER);
        heart.setBackground(background(
            Color.rgb(232, 245, 233), 50
        ));

        nameRow.addView(heart,
            new LinearLayout.LayoutParams(dp(42), dp(42))
        );

        details.addView(nameRow);

        // Original Price
        TextView priceText = makeText(
            price, 23, GREEN
        );
        priceText.setTypeface(null, Typeface.BOLD);
        priceText.setPadding(0, dp(5), 0, dp(4));

        details.addView(priceText);

        // Pack Size
        TextView packText = makeText(
            pack, 13, Color.rgb(35, 110, 60)
        );
        packText.setPadding(dp(9), dp(5), dp(9), dp(5));
        packText.setBackground(background(
            LIGHT_GREEN, 20
        ));

        details.addView(packText);
        card.addView(top);

        // Add to Cart
        Button addButton = new Button(form);
        addButton.setText("🛒  Add to Cart");
        addButton.setTextSize(15);
        addButton.setTextColor(Color.WHITE);
        addButton.setAllCaps(false);
        addButton.setTypeface(null, Typeface.BOLD);
        addButton.setBackground(background(GREEN, 30));

        LinearLayout.LayoutParams buttonParams =
            new LinearLayout.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT,
                dp(50)
            );

        buttonParams.setMargins(0, dp(12), 0, 0);
        card.addView(addButton, buttonParams);

        // Quantity Controls
        LinearLayout qtyRow = new LinearLayout(form);
        qtyRow.setOrientation(LinearLayout.HORIZONTAL);
        qtyRow.setGravity(Gravity.CENTER);
        qtyRow.setVisibility(View.GONE);

        Button minus = new Button(form);
        minus.setText("−");
        minus.setTextSize(22);
        minus.setTextColor(Color.WHITE);
        minus.setAllCaps(false);
        minus.setBackground(background(
            Color.rgb(190, 45, 45), 30
        ));

        TextView qtyText = makeText("1", 21, GREEN);
        qtyText.setGravity(Gravity.CENTER);
        qtyText.setBackground(background(LIGHT_GREEN, 12));

        Button plus = new Button(form);
        plus.setText("+");
        plus.setTextSize(22);
        plus.setTextColor(Color.WHITE);
        plus.setAllCaps(false);
        plus.setBackground(background(GREEN, 30));

        qtyRow.addView(minus,
            new LinearLayout.LayoutParams(0, dp(48), 1)
        );

        LinearLayout.LayoutParams numberParams =
            new LinearLayout.LayoutParams(0, dp(48), 2);

        numberParams.setMargins(dp(10), 0, dp(10), 0);
        qtyRow.addView(qtyText, numberParams);

        qtyRow.addView(plus,
            new LinearLayout.LayoutParams(0, dp(48), 1)
        );

        LinearLayout.LayoutParams qtyParams =
            new LinearLayout.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT,
                dp(50)
            );

        qtyParams.setMargins(0, dp(12), 0, 0);
        card.addView(qtyRow, qtyParams);

        // Total Price
        TextView total = makeText(
            "Total Price: ₹0.00", 17, GREEN
        );

        total.setTypeface(null, Typeface.BOLD);
        total.setGravity(Gravity.CENTER_VERTICAL);
        total.setPadding(dp(10), dp(10), dp(10), dp(10));
        total.setBackground(background(LIGHT_GREEN, 12));
        total.setVisibility(View.GONE);

        LinearLayout.LayoutParams totalParams =
            new LinearLayout.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT,
                ViewGroup.LayoutParams.WRAP_CONTENT
            );

        totalParams.setMargins(0, dp(8), 0, 0);
        card.addView(total, totalParams);

        // Save References
        cards.put(id, card);
        cardParents.put(id, container);
        titles.put(id, name);
        prices.put(id, priceText);
        packs.put(id, packText);
        hearts.put(id, heart);
        images.put(id, image);
        quantitiesText.put(id, qtyText);
        totals.put(id, total);
        addButtons.put(id, addButton);
        quantityRows.put(id, qtyRow);

        quantities.put(id, 0);
        unitPrices.put(id, parsePrice(price));
        favorites.put(id, false);
        imageRequests.put(id, 0);

        // Favorite Click
        heart.setOnClickListener(v -> {
            boolean selected = !Boolean.TRUE.equals(
                favorites.get(id)
            );

            favorites.put(id, selected);

            heart.setText(selected ? "♥" : "♡");
            heart.setTextColor(
                selected ? Color.WHITE : GREEN
            );

            heart.setBackground(background(
                selected ? GREEN : LIGHT_GREEN, 50
            ));

            FavoriteChanged(id, selected);
        });

        // Add to Cart
        addButton.setOnClickListener(v -> {
            quantities.put(id, 1);
            qtyText.setText("1");

            addButton.setVisibility(View.GONE);
            qtyRow.setVisibility(View.VISIBLE);

            updateTotal(id);

            AddToCartClicked(id);
            QuantityChanged(id, 1);
            TotalPriceChanged(id, getTotal(id));
        });

        // Plus Button
        plus.setOnClickListener(v -> {
            Integer oldQty = quantities.get(id);
            int qty = oldQty == null ? 1 : oldQty + 1;

            quantities.put(id, qty);
            qtyText.setText(String.valueOf(qty));

            updateTotal(id);

            QuantityChanged(id, qty);
            TotalPriceChanged(id, getTotal(id));
        });

        // Minus Button
        minus.setOnClickListener(v -> {
            Integer oldQty = quantities.get(id);
            int qty = oldQty == null ? 0 : oldQty - 1;

            if (qty <= 0) {
                qty = 0;
                qtyRow.setVisibility(View.GONE);
                addButton.setVisibility(View.VISIBLE);
            }

            quantities.put(id, qty);
            qtyText.setText(String.valueOf(qty));

            updateTotal(id);

            QuantityChanged(id, qty);
            TotalPriceChanged(id, getTotal(id));
        });

        // Add Card to Vertical Parent
        LinearLayout.LayoutParams cardParams =
            new LinearLayout.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT,
                ViewGroup.LayoutParams.WRAP_CONTENT
            );

        cardParams.setMargins(
            dp(6), dp(7), dp(6), dp(7)
        );

        container.addView(card, cardParams);

        CardCreated(id);
    }

    @SimpleFunction(description = "Set product image using URL.")
    public void SetImage(String id, String imageUrl) {
        final ImageView image = images.get(id);

        if (image == null || imageUrl == null) return;

        Integer oldRequest = imageRequests.get(id);
        final int request = (oldRequest == null ? 0 : oldRequest) + 1;
        imageRequests.put(id, request);

        new Thread(() -> {
            HttpURLConnection connection = null;

            try {
                URL url = new URL(imageUrl);
                connection = (HttpURLConnection) url.openConnection();

                connection.setConnectTimeout(10000);
                connection.setReadTimeout(10000);
                connection.setDoInput(true);
                connection.connect();

                Bitmap bitmap;

                try (InputStream input = connection.getInputStream()) {
                    bitmap = BitmapFactory.decodeStream(input);
                }

                if (bitmap == null) {
                    throw new Exception("Invalid image");
                }

                final Bitmap result = bitmap;

                form.runOnUiThread(() -> {
                    if (images.get(id) == image &&
                        Integer.valueOf(request).equals(imageRequests.get(id))) {
                        image.setImageBitmap(result);
                    }
                });

            } catch (Exception e) {
                final String error = e.getMessage();

                form.runOnUiThread(() -> {
                    if (Integer.valueOf(request).equals(imageRequests.get(id))) {
                        ImageError(id, error == null
                            ? "Image loading failed" : error);
                    }
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
        TextView view = titles.get(id);
        if (view != null) view.setText(title);
    }

    @SimpleFunction(description = "Change original unit price.")
    public void SetPrice(String id, String price) {
        TextView view = prices.get(id);
        if (view == null) return;

        view.setText(price);
        unitPrices.put(id, parsePrice(price));

        updateTotal(id);
        TotalPriceChanged(id, getTotal(id));
    }

    @SimpleFunction(description = "Change product pack size.")
    public void SetPack(String id, String pack) {
        TextView view = packs.get(id);
        if (view != null) view.setText(pack);
    }

    @SimpleFunction(description = "Get current quantity.")
    public int GetQuantity(String id) {
        Integer qty = quantities.get(id);
        return qty == null ? 0 : qty;
    }

    @SimpleFunction(description = "Get total price.")
    public double GetTotalPrice(String id) {
        return getTotal(id);
    }

    @SimpleFunction(description = "Check favorite status.")
    public boolean IsFavorite(String id) {
        return Boolean.TRUE.equals(favorites.get(id));
    }

    @SimpleFunction(description = "Remove a product card.")
    public void RemoveCard(String id) {
        View card = cards.get(id);

        if (card != null && card.getParent() instanceof ViewGroup) {
            ((ViewGroup) card.getParent()).removeView(card);
        }

        cards.remove(id);
        cardParents.remove(id);
        titles.remove(id);
        prices.remove(id);
        packs.remove(id);
        hearts.remove(id);
        images.remove(id);
        quantitiesText.remove(id);
        totals.remove(id);
        addButtons.remove(id);
        quantityRows.remove(id);
        quantities.remove(id);
        unitPrices.remove(id);
        favorites.remove(id);
        imageRequests.remove(id);
    }

    @SimpleFunction(description = "Remove all cards from the given parent.")
    public void ClearCards(HVArrangement parent) {
        if (parent == null) return;

        View parentView = parent.getView();
        if (!(parentView instanceof ViewGroup)) return;

        ViewGroup target = (ViewGroup) parentView;

        ArrayList<String> ids = new ArrayList<>();

        for (String id : cards.keySet()) {
            if (cardParents.get(id) == target) {
                ids.add(id);
            }
        }

        for (String id : ids) {
            RemoveCard(id);
        }
    }

    @SimpleEvent(description = "Card created.")
    public void CardCreated(String id) {
        EventDispatcher.dispatchEvent(this, "CardCreated", id);
    }

    @SimpleEvent(description = "Add to cart clicked.")
    public void AddToCartClicked(String id) {
        EventDispatcher.dispatchEvent(this, "AddToCartClicked", id);
    }

    @SimpleEvent(description = "Quantity changed.")
    public void QuantityChanged(String id, int quantity) {
        EventDispatcher.dispatchEvent(
            this, "QuantityChanged", id, quantity
        );
    }

    @SimpleEvent(description = "Favorite changed.")
    public void FavoriteChanged(String id, boolean isFavorite) {
        EventDispatcher.dispatchEvent(
            this, "FavoriteChanged", id, isFavorite
        );
    }

    @SimpleEvent(description = "Total price changed.")
    public void TotalPriceChanged(String id, double totalPrice) {
        EventDispatcher.dispatchEvent(
            this, "TotalPriceChanged", id, totalPrice
        );
    }

    @SimpleEvent(description = "Image loading failed.")
    public void ImageError(String id, String message) {
        EventDispatcher.dispatchEvent(
            this, "ImageError", id, message
        );
    }
}
