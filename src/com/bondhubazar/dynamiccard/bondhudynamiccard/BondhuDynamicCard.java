
private LinearLayout createProductCard(
        Context context,
        String name,
        String pack,
        String price,
        int imageRes
) {

    LinearLayout card = new LinearLayout(context);
    card.setOrientation(LinearLayout.VERTICAL);
    card.setPadding(6, 6, 6, 6);
    card.setBackgroundColor(Color.WHITE);

    LinearLayout.LayoutParams cardParams =
        new LinearLayout.LayoutParams(
            dp(context, 100),
            dp(context, 200)
        );

    cardParams.setMargins(4, 4, 4, 4);
    card.setLayoutParams(cardParams);

    // Image
    FrameLayout imageBox = new FrameLayout(context);

    ImageView image = new ImageView(context);
    image.setImageResource(imageRes);
    image.setScaleType(ImageView.ScaleType.CENTER_CROP);

    imageBox.addView(image,
        new FrameLayout.LayoutParams(
            -1, dp(context, 105)
        )
    );

    // Favorite button
    TextView favorite = new TextView(context);
    favorite.setText("♡");
    favorite.setTextSize(24);
    favorite.setTextColor(Color.RED);
    favorite.setGravity(Gravity.CENTER);

    FrameLayout.LayoutParams favParams =
        new FrameLayout.LayoutParams(
            dp(context, 30),
            dp(context, 30),
            Gravity.TOP | Gravity.RIGHT
        );

    imageBox.addView(favorite, favParams);
    card.addView(imageBox);

    // Product Name
    TextView productName = new TextView(context);
    productName.setText(name);
    productName.setTextSize(13);
    productName.setTextColor(Color.BLACK);
    productName.setMaxLines(1);
    card.addView(productName);

    // Pack Size
    TextView packSize = new TextView(context);
    packSize.setText(pack);
    packSize.setTextSize(11);
    packSize.setTextColor(Color.GRAY);
    card.addView(packSize);

    // Price
    TextView productPrice = new TextView(context);
    productPrice.setText("₹" + price);
    productPrice.setTextSize(16);
    productPrice.setTextColor(Color.rgb(20, 140, 60));
    card.addView(productPrice);

    return card;
}

private int dp(Context context, int value) {
    return (int) (value *
        context.getResources()
        .getDisplayMetrics().density + 0.5f);
}
