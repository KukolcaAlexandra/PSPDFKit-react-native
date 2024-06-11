package com.pspdfkit.react;

import android.content.Context;
import android.content.Intent;
import android.content.res.TypedArray;
import android.graphics.drawable.Drawable;
import android.net.Uri;
import android.view.Menu;
import android.view.MenuItem;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.core.content.ContextCompat;
import androidx.core.graphics.drawable.DrawableCompat;

import com.pspdfkit.configuration.activity.PdfActivityConfiguration;
import com.pspdfkit.ui.PdfActivity;
import com.pspdfkit.ui.PdfActivityIntentBuilder;
import com.pspdfkit.views.PdfView;

import java.util.List;

import kotlin.jvm.internal.Intrinsics;

public class PdfActivityWithRotate extends PdfActivity {

  public static void showDocument(@NonNull Context context, @NonNull Uri documentUri, @Nullable String password, @Nullable PdfActivityConfiguration configuration) {
    PdfActivityIntentBuilder builder = PdfActivityIntentBuilder.fromUri(context, new Uri[]{documentUri});
    builder.activityClass(PdfActivityWithRotate.class);
    Intent intent = builder.passwords(new String[]{password}).configuration(configuration).build();

    context.startActivity(intent);
  }

  /**
   * Override this method to get the list of menu item IDs, as they'll be ordered by default.
   * You can add your own menu item IDs that you can later edit in `{@link #onCreateOptionsMenu(Menu)}`
   * or `{@link #onPrepareOptionsMenu(Menu)}`.
   */
  @NonNull
  @Override
  public List<Integer> onGenerateMenuItemIds(@NonNull List<Integer> menuItems) {
    // For example let's say we want to add custom menu items after the outline button.
    // First, we get an index of outline buttons (all default button IDs can be retrieved
    // via `MENU_OPTION_*` variables defined in the `PdfActivity`.
    int indexOfOutlineButton = menuItems.indexOf(MENU_OPTION_OUTLINE);

    // Add a custom item after the outline button.
    menuItems.add(indexOfOutlineButton + 1, R.id.rotate_action);

    // Return the new menu items order.
    return menuItems;
  }

  @Override
  public boolean onOptionsItemSelected(@NonNull MenuItem item) {
    if (item.getItemId() == R.id.rotate_action) {
      PdfView view = (PdfView) this.findViewById(android.R.id.content);
      view.rotatePage(0, 0);
      return true;
    }

    return super.onOptionsItemSelected(item);
  }

  @Override
  public boolean onCreateOptionsMenu(Menu menu) {
    // This will populate the menu with items ordered as specified in `onGenerateMenuItemIds()`.
    super.onCreateOptionsMenu(menu);

    // Edit the custom button.
    MenuItem customMenuItem = menu.findItem(R.id.rotate_action);
    customMenuItem.setTitle("Rotate");
    customMenuItem.setIcon(R.drawable.ic_rotate);

    // Let's say we want the icon to be tinted the same color as the default ones. We can read the color
    // from the theme, or we can specify the same color we have in the theme. Reading from the theme is a bit
    // more complex, but this a better way to do it, so here's how:
    final TypedArray a = getTheme().obtainStyledAttributes(
      null,
      R.styleable.pspdf__ActionBarIcons,
      R.attr.pspdf__actionBarIconsStyle,
      R.style.PSPDFKit_ActionBarIcons
    );
    int mainToolbarIconsColor = a.getColor(R.styleable.pspdf__ActionBarIcons_pspdf__iconsColor, ContextCompat.getColor(this, R.color.material_on_primary_emphasis_high_type));
    a.recycle();

    // Tinting custom menu drawable.
    Drawable customIcon = customMenuItem.getIcon();
    DrawableCompat.setTint(customIcon, mainToolbarIconsColor);
    customMenuItem.setIcon(customIcon);

    // All our menu items are marked as `SHOW_AS_ALWAYS`. If you want to, for example,
    // just show the first four items and send the others to the overflow, you can simply do:
    for (int i = 0; i < menu.size(); i++) {
      menu.getItem(i).setShowAsAction(i < 4 ? MenuItem.SHOW_AS_ACTION_ALWAYS : MenuItem.SHOW_AS_ACTION_NEVER);
    }

    return true;
  }

  @Override
  public boolean onPrepareOptionsMenu(Menu menu) {
    // Here, you can edit your items when the menu is being invalidated.
    // To invalidate the menu, call `supportInvalidateOptionsMenu();`
    return super.onPrepareOptionsMenu(menu);
  }
}
