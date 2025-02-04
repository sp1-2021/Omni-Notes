/*
 * Copyright (C) 2013-2020 Federico Iosue (federico@iosue.it)
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */

package it.feio.android.omninotes.models.views;

import android.content.Context;
import android.util.AttributeSet;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ListAdapter;
import android.widget.ListView;


public class NonScrollableListView extends ListView {

  public NonScrollableListView(Context context) {
    super(context);
  }


  public NonScrollableListView(Context context, AttributeSet attrs) {
    super(context, attrs);
  }


  public NonScrollableListView(Context context, AttributeSet attrs, int defStyleAttr) {
    super(context, attrs, defStyleAttr);
  }


  public void justifyListViewHeightBasedOnChildren() {

    ListAdapter adapter = getAdapter();

    if (adapter == null) {
      return;
    }
    ViewGroup vg = this;
    int totalHeight = 0;
    for (int i = 0; i < adapter.getCount(); i++) {
      View listItem = adapter.getView(i, null, vg);
      listItem.measure(0, 0);
      totalHeight += listItem.getMeasuredHeight();
    }

    ViewGroup.LayoutParams par = getLayoutParams();
    par.height = totalHeight + (getDividerHeight() * (adapter.getCount() - 1));
    setLayoutParams(par);
    requestLayout();
  }
}
