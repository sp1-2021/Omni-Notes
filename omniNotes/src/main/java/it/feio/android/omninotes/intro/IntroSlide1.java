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

package it.feio.android.omninotes.intro;

import android.graphics.Color;
import android.os.Bundle;
import android.view.View;
import it.feio.android.omninotes.R;


public class IntroSlide1 extends IntroFragment {

  @Override
  public void onActivityCreated(Bundle savedInstanceState) {
    super.onActivityCreated(savedInstanceState);
    binding.introBackground.setBackgroundColor(Color.parseColor("#222222"));
    binding.introTitle.setText(R.string.tour_listactivity_intro_title);
    binding.introImage.setVisibility(View.GONE);
    binding.introImageSmall.setImageResource(R.drawable.logo);
    binding.introImageSmall.setVisibility(View.VISIBLE);
    binding.introDescription.setText(R.string.tour_listactivity_final_detail);
  }
}
