package ch.FOW_Collection.presentation.cardDetails;

import android.app.ActivityOptions;
import android.content.Intent;
import android.graphics.Color;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.view.MenuItem;
import android.view.View;
import android.widget.ImageView;
import android.widget.RatingBar;
import android.widget.TextView;
import android.widget.ToggleButton;

import com.bumptech.glide.load.DataSource;
import com.bumptech.glide.load.engine.GlideException;
import com.bumptech.glide.request.RequestListener;
import com.bumptech.glide.request.RequestOptions;
import com.bumptech.glide.request.target.Target;
import com.google.android.material.bottomsheet.BottomSheetDialog;
import com.google.firebase.storage.FirebaseStorage;

import java.util.ArrayList;
import java.util.List;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.core.widget.NestedScrollView;
import androidx.lifecycle.ViewModelProviders;
import androidx.recyclerview.widget.DividerItemDecoration;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;
import ch.FOW_Collection.GlideApp;
import ch.FOW_Collection.R;
import ch.FOW_Collection.domain.models.Card;
import ch.FOW_Collection.domain.models.Rating;
import ch.FOW_Collection.domain.models.Wish;
import ch.FOW_Collection.presentation.cardDetails.createrating.CreateRatingActivity;
import ch.FOW_Collection.presentation.shared.CardImageLoader;

import static ch.FOW_Collection.presentation.utils.DrawableHelpers.setDrawableTint;

public class CardDetailsActivity extends AppCompatActivity implements OnRatingLikedListener {

    public static final String ITEM_ID = "item_id";
    private static final String TAG = "CardDetailsActivity";
    @BindView(R.id.toolbar)
    Toolbar toolbar;

    @BindView(R.id.nested_scroll_view)
    NestedScrollView nestedScrollView;

    @BindView(R.id.imageCard)
    ImageView imageView;

    @BindView(R.id.avgRating)
    TextView avgRating;

    @BindView(R.id.numRatings)
    TextView numRatings;

    @BindView(R.id.ratingBar)
    RatingBar ratingBar;

    @BindView(R.id.name)
    TextView name;

    @BindView(R.id.wishlist)
    ToggleButton wishlist;

    @BindView(R.id.manufacturer)
    TextView manufacturer;

    @BindView(R.id.category)
    TextView category;

    @BindView(R.id.addRatingBar)
    RatingBar addRatingBar;

    @BindView(R.id.recyclerView)
    RecyclerView recyclerView;

    private RatingsRecyclerViewAdapter adapter;

    private CardDetailsViewModel model;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_card_details);
        ButterKnife.bind(this);
        setSupportActionBar(toolbar);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);

        getWindow().getDecorView()
                .setSystemUiVisibility(View.SYSTEM_UI_FLAG_LAYOUT_STABLE | View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN);
        toolbar.setTitleTextColor(Color.alpha(0));

        String cardId = getIntent().getExtras().getString(ITEM_ID);

        model = ViewModelProviders.of(this).get(CardDetailsViewModel.class);
        model.setCardId(cardId);

        LinearLayoutManager layoutManager = new LinearLayoutManager(this);
        recyclerView.setLayoutManager(layoutManager);

        adapter = new RatingsRecyclerViewAdapter(this, model.getCurrentUser());
        recyclerView.addItemDecoration(new DividerItemDecoration(this, layoutManager.getOrientation()));

        model.getCard().observe(this, this::updateCard);
        //model.getRatings().observe(this, this::updateRatings);
        //model.getWish().observe(this, this::toggleWishlistView);

        recyclerView.setAdapter(adapter);
        addRatingBar.setOnRatingBarChangeListener(this::addNewRating);
    }

    private void addNewRating(RatingBar ratingBar, float v, boolean b) {
        Intent intent = new Intent(this, CreateRatingActivity.class);
        intent.putExtra(CreateRatingActivity.ITEM, model.getCard().getValue());
        intent.putExtra(CreateRatingActivity.RATING, v);
        ActivityOptions options = ActivityOptions.makeSceneTransitionAnimation(this, addRatingBar, "rating");
        startActivity(intent, options.toBundle());
    }

    @OnClick(R.id.actionsButton)
    public void showBottomSheetDialog() {
        View view = getLayoutInflater().inflate(R.layout.single_bottom_sheet_dialog, null);
        BottomSheetDialog dialog = new BottomSheetDialog(this);
        dialog.setContentView(view);
        dialog.show();
    }

    private void updateCard(Card item) {
        name.setText(item.getName().getDe());

        item.getEdition().observe(this, cardEdition -> {
            manufacturer.setText(cardEdition.getDe());
        });



        category.setText(item.getId());
        // name.setText(item.getName());
        //GlideApp.with(this).load(item.getPhoto()).apply(new RequestOptions().override(120, 160).centerInside())
        //        .into(imageView);
        GlideApp.with(this)
                .load(FirebaseStorage.getInstance().getReference().child(item.getImageStorageUrl()))
                .apply(new RequestOptions().centerInside())
                .apply(CardImageLoader.imageRenderer)
                .dontAnimate()
                .listener(new RequestListener<Drawable>() {
                    @Override
                    public boolean onLoadFailed(@Nullable GlideException e, Object model, Target<Drawable> target, boolean isFirstResource) {
                        supportStartPostponedEnterTransition();
                        return false;
                    }

                    @Override
                    public boolean onResourceReady(Drawable resource, Object model, Target<Drawable> target, DataSource dataSource, boolean isFirstResource) {
                        supportStartPostponedEnterTransition();
                        return false;
                    }
                })
                .into(imageView);

        ratingBar.setNumStars(5);
        ratingBar.setRating(item.getAvgRating());
        avgRating.setText(getResources().getString(R.string.fmt_avg_rating, item.getAvgRating()));
        numRatings.setText(getResources().getString(R.string.fmt_ratings, item.getNumRatings()));
        toolbar.setTitle(item.getName().getDe());
    }

    private void updateRatings(List<Rating> ratings) {
        adapter.submitList(new ArrayList<>(ratings));
    }

    @Override
    public void onRatingLikedListener(Rating rating) {
        // model.toggleLike(rating);
    }

    @OnClick(R.id.wishlist)
    public void onWishClickedListener(View view) {
        //model.toggleItemInWishlist(model.getCard().getValue().getId());
        /*
         * We won't get an update from firestore when the wish is removed, so we need to reset the UI state ourselves.
         * */
        if (!wishlist.isChecked()) {
            toggleWishlistView(null);
        }
    }

    private void toggleWishlistView(Wish wish) {
        if (wish != null) {
            int color = getResources().getColor(R.color.colorPrimary);
            setDrawableTint(wishlist, color);
            wishlist.setChecked(true);
        } else {
            int color = getResources().getColor(android.R.color.darker_gray);
            setDrawableTint(wishlist, color);
            wishlist.setChecked(false);
        }
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case android.R.id.home:
                supportFinishAfterTransition();
                return true;
            default:
                return super.onOptionsItemSelected(item);
        }
    }
}
