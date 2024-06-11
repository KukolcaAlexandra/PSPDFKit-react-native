/*
 * ReactPdfUiFragment.java
 *
 *   PSPDFKit
 *
 *   Copyright © 2021-2024 PSPDFKit GmbH. All rights reserved.
 *
 *   THIS SOURCE CODE AND ANY ACCOMPANYING DOCUMENTATION ARE PROTECTED BY INTERNATIONAL COPYRIGHT LAW
 *   AND MAY NOT BE RESOLD OR REDISTRIBUTED. USAGE IS BOUND TO THE PSPDFKIT LICENSE AGREEMENT.
 *   UNAUTHORIZED REPRODUCTION OR DISTRIBUTION IS SUBJECT TO CIVIL AND CRIMINAL PENALTIES.
 *   This notice may not be removed from this file.
 */

package com.pspdfkit.views;

import android.content.Context;
import android.content.res.TypedArray;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.os.Handler;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;

import androidx.annotation.DrawableRes;
import androidx.annotation.IdRes;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.annotation.StringRes;
import androidx.appcompat.widget.Toolbar;
import androidx.core.content.ContextCompat;
import androidx.core.graphics.drawable.DrawableCompat;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;

import com.pspdfkit.document.PdfDocument;
import com.pspdfkit.document.editor.PdfDocumentEditor;
import com.pspdfkit.document.editor.PdfDocumentEditorFactory;
import com.pspdfkit.react.R;
import com.pspdfkit.ui.PdfActivity;
import com.pspdfkit.react.helper.PSPDFKitUtils;
import com.pspdfkit.ui.PdfFragment;
import com.pspdfkit.ui.PdfUiFragment;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.HashMap;
import java.util.List;
import java.util.Objects;

import io.reactivex.rxjava3.android.schedulers.AndroidSchedulers;
import io.reactivex.rxjava3.disposables.Disposable;
import io.reactivex.rxjava3.schedulers.Schedulers;

/**
 * This {@link PdfUiFragment} provides additional callbacks to improve integration into react native.
 * <p/>
 * <ul>
 * <li>A callback when the configuration was changed.</li>
 * <li>A method to show and hide the navigation button in the toolbar, as well as a callback for when it is clicked.</li>
 * </ul>
 */
public class ReactPdfUiFragment extends PdfUiFragment {

    private ArrayList<HashMap> customToolbarItems = new ArrayList<>();
    private MenuItemListener menuItemListener;

    @Nullable private ReactPdfUiFragmentListener reactPdfUiFragmentListener;

    private final FragmentManager.FragmentLifecycleCallbacks fragmentLifecycleCallbacks = new FragmentManager.FragmentLifecycleCallbacks() {
        @Override
        public void onFragmentCreated(@NonNull FragmentManager fm, @NonNull Fragment f, @Nullable Bundle savedInstanceState) {
            super.onFragmentCreated(fm, f, savedInstanceState);
            // Whenever a new PdfFragment is created that means the configuration has changed.
            if (f instanceof PdfFragment) {
                if (reactPdfUiFragmentListener != null) {
                    reactPdfUiFragmentListener.onConfigurationChanged(ReactPdfUiFragment.this);
                }
            }
        }
    };

    void setReactPdfUiFragmentListener(@Nullable ReactPdfUiFragmentListener listener) {
        this.reactPdfUiFragmentListener = listener;
    }

    /** When set to true will add a navigation arrow to the toolbar. */
    void setShowNavigationButtonInToolbar(final boolean showNavigationButtonInToolbar) {
        if (getView() == null) {
          return;
        }
    
        Toolbar toolbar = getView().findViewById(R.id.pspdf__toolbar_main);
        if (showNavigationButtonInToolbar) {
            toolbar.setNavigationIcon(R.drawable.ic_download);
            toolbar.setNavigationIcon(
                getStyledIcon(Objects.requireNonNull(
                    toolbar.getNavigationIcon())));
          toolbar.setNavigationOnClickListener(v -> downloadPdf());
        } else {
            toolbar.setNavigationIcon(null);
            toolbar.setNavigationOnClickListener(null);
        }
      }
    
      private int pageIndex = -1;

      @Override
      public void onDocumentLoaded(@NonNull PdfDocument document) {
        if (pageIndex >= 0) {
          new Handler().post(() -> {
            setPageIndex(pageIndex, false);
          });
        }
      }

    @Override
    public void onStart() {
        super.onStart();
        // We want to get notified when a child PdfFragment is created so we can reattach our listeners.
        getChildFragmentManager().registerFragmentLifecycleCallbacks(fragmentLifecycleCallbacks, false);
    }

    @Override
    public void onStop() {
        super.onStop();
        getChildFragmentManager().unregisterFragmentLifecycleCallbacks(fragmentLifecycleCallbacks);
    }

    /**
     * Listener that notifies of actions taken directly in the PdfUiFragment.
     */
    public interface ReactPdfUiFragmentListener {

        /**
         * Called when the configuration changed, reset your {@link com.pspdfkit.ui.PdfFragment} and {@link PdfUiFragment} listeners in here.
         */
        void onConfigurationChanged(@NonNull PdfUiFragment pdfUiFragment);
    
        void onStartDownload();
    
        void reloadData();
    }

    void setCustomToolbarItems(@NonNull ArrayList<HashMap> customToolbarItems, MenuItemListener listener) {
        this.customToolbarItems = customToolbarItems;
        this.menuItemListener = listener;
    }

    @NonNull
  @Override
  public List<Integer> onGenerateMenuItemIds(@NonNull List<Integer> menuItems) {
    menuItems.clear();
    menuItems.add(PdfActivity.MENU_OPTION_SEARCH);
    menuItems.add(R.id.menu_action_rotate);
    return menuItems;
  }

  private Drawable getStyledIcon(@NonNull Drawable icon) {
    final TypedArray a = requireContext().getTheme().obtainStyledAttributes(
      null,
      com.pspdfkit.R.styleable.pspdf__ActionBarIcons,
      com.pspdfkit.R.attr.pspdf__actionBarIconsStyle,
      com.pspdfkit.R.style.PSPDFKit_ActionBarIcons
    );
    int mainToolbarIconsColor = a.getColor(com.pspdfkit.R.styleable.pspdf__ActionBarIcons_pspdf__iconsColor,
      ContextCompat.getColor(requireContext(), android.R.color.white));
    a.recycle();
    DrawableCompat.setTint(icon, mainToolbarIconsColor);
    return icon;
  }

  private void addMenuItem(@NonNull Menu menu,
                           @IdRes int menuId,
                           @StringRes int titleId,
                           @DrawableRes int iconId,
                           int order,
                           MenuItem.OnMenuItemClickListener menuItemListener) {

    MenuItem item = menu.add(Menu.NONE, menuId, order, titleId);
    item.setShowAsAction(MenuItem.SHOW_AS_ACTION_ALWAYS);
    item.setIcon(iconId);
    item.setOnMenuItemClickListener(menuItemListener);

    item.setIcon(getStyledIcon(Objects.requireNonNull(item.getIcon())));
  }

  private void downloadPdf() {
    if (reactPdfUiFragmentListener != null) {
      reactPdfUiFragmentListener.onStartDownload();
    }
  }

  private void rotateDocument() {
    PdfDocument document = Objects.requireNonNull(getDocument());
    pageIndex = getPageIndex();
    List<Integer> pagesList = new ArrayList<>(document.getPageCount());
    for (int i = 0; i < document.getPageCount(); i++) {
      pagesList.add(i);
    }
    final HashSet<Integer> pagesToRotate = new HashSet<>(pagesList);

    final PdfDocumentEditor documentEditor =
      PdfDocumentEditorFactory.createForDocument(document);

    final Context context = requireContext();

    final Disposable disposable = documentEditor.rotatePages(pagesToRotate, PdfDocument.ROTATION_90)
      .flatMapCompletable(saved -> documentEditor.saveDocument(context, null))
      .subscribeOn(Schedulers.io())
      .observeOn(AndroidSchedulers.mainThread())
      .subscribe(() -> {
        if (reactPdfUiFragmentListener != null) {
          reactPdfUiFragmentListener.reloadData();
        }
      }, (error) -> {
        error.printStackTrace();
      });
  }

  @Override
  public void onCreateOptionsMenu(@NonNull Menu menu, @NonNull MenuInflater inflater) {
    super.onCreateOptionsMenu(menu, inflater);
    MenuItem searchMenuItem = menu.findItem(PdfActivity.MENU_OPTION_SEARCH);
    menu.clear();

    menu.add(Menu.NONE, PdfActivity.MENU_OPTION_SEARCH, 1, searchMenuItem.getTitle());

    addMenuItem(menu, R.id.menu_action_rotate, R.string.rotate, R.drawable.ic_rotate,
      2, item -> {
        rotateDocument();
        return true;
      });
  }
}
