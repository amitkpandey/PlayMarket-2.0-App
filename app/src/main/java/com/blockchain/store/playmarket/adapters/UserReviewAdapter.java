package com.blockchain.store.playmarket.adapters;

import android.animation.ObjectAnimator;
import android.support.annotation.NonNull;
import android.support.constraint.ConstraintLayout;
import android.support.v7.widget.RecyclerView;
import android.text.Layout;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.blockchain.store.playmarket.R;
import com.blockchain.store.playmarket.data.entities.UserReview;
import com.blockchain.store.playmarket.utilities.AccountManager;
import com.blockchain.store.playmarket.utilities.Constants;

import java.util.ArrayList;

import butterknife.BindView;
import butterknife.ButterKnife;
import me.zhanghai.android.materialratingbar.MaterialRatingBar;

public class UserReviewAdapter extends RecyclerView.Adapter<RecyclerView.ViewHolder> {
    private static final String TAG = "UserReviewAdapter";
    private static final int TYPE_USER_REPLY = 0;
    private static final int TYPE_USER_REVIEW = 1;
    ArrayList<UserReview> userReviews;
    int expandedItem = -1;
    UserReviewCallback callback;
    String userAddress;

    public UserReviewAdapter(ArrayList<UserReview> userReviews, UserReviewCallback callback) {
        this.callback = callback;
        this.userReviews = userReviews;
        userAddress = AccountManager.getAddress().getHex();
    }

    @Override
    public int getItemViewType(int position) {
        if (!userReviews.get(position).author.equalsIgnoreCase(userAddress)) {
            return TYPE_USER_REVIEW;
        } else {
            return TYPE_USER_REPLY;
        }
    }

    @NonNull
    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        if (viewType == TYPE_USER_REVIEW) {
            View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.user_review_item, parent, false);
            return new UserReviewViewHolder(view, viewType);
        } else if (viewType == TYPE_USER_REPLY) {
            View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.user_replay_item, parent, false);
            return new UserReviewViewHolder(view, viewType);
        }
        return null;
    }

    @Override
    public void onBindViewHolder(@NonNull RecyclerView.ViewHolder holder, int position) {
        if (holder instanceof UserReviewViewHolder) {
            ((UserReviewViewHolder) holder).bind(userReviews.get(position), position);
        }
    }

    @Override
    public int getItemCount() {
        return userReviews.size();
    }

    class UserReviewViewHolder extends RecyclerView.ViewHolder {
        @BindView(R.id.user_name) TextView userName;
        @BindView(R.id.user_rating_bar) MaterialRatingBar userRatingBar;
        @BindView(R.id.user_commentary) TextView userCommentary;
        @BindView(R.id.read_more) TextView readMore;
        @BindView(R.id.root_view) ConstraintLayout constraintLayout;

        private ObjectAnimator textDescriptionAnimator;
        private View itemView;
        private int viewType;

        public UserReviewViewHolder(View itemView, int viewType) {
            super(itemView);
            this.itemView = itemView;
            ButterKnife.bind(this, itemView);
            this.viewType = viewType;
            userCommentary.post(() -> setupViewAfterOnMeasure(userCommentary));
        }

        public void bind(UserReview userReview, int position) {
            ViewGroup.MarginLayoutParams contraintParmas = (ViewGroup.MarginLayoutParams) constraintLayout.getLayoutParams();
            constraintLayout.setOnClickListener(v -> callback.onReplyOnReviewClicked(userReview));

//            if (viewType == TYPE_USER_REVIEW) {
//                contraintParmas.setMargins();
//            } else {
//
//            }
//
//            if (userReview.isReviewOnReview) {
//
//            } else {
//
//            }
            userCommentary.setText(userReview.text);
            userName.setText(userReview.author);
            try {
                userRatingBar.setRating(Float.parseFloat(userReview.rating));
            } catch (Exception e) {

            }
        }

        private void setupViewAfterOnMeasure(TextView userCommentary) {
            Layout userCommentaryLayout = userCommentary.getLayout();
            if (userCommentaryLayout != null) {
                int lineCount = userCommentaryLayout.getLineCount();
                int ellipsisCount = userCommentaryLayout.getEllipsisCount(lineCount - 1);
                if (ellipsisCount > 0) {
                    readMore.setVisibility(View.VISIBLE);
                }
                Log.d(TAG, "UserReviewViewHolder: ellipsisCount " + ellipsisCount);
            }
            readMore.setOnClickListener(v -> setReadMoreLogic(userCommentary));
        }

        private void setReadMoreLogic(TextView userCommentary) {
            if (userCommentary.getMaxLines() == 2) {
                textDescriptionAnimator = ObjectAnimator.ofInt(userCommentary, "maxLines", 10);
                readMore.setText(itemView.getContext().getString(R.string.read_less));
            } else if (userCommentary.getMaxLines() == 10) {
                textDescriptionAnimator = ObjectAnimator.ofInt(userCommentary, "maxLines", 2);
                readMore.setText(itemView.getContext().getString(R.string.more));
            }

            if (textDescriptionAnimator != null && !textDescriptionAnimator.isStarted()) {
                textDescriptionAnimator.setDuration(Constants.USER_REVIEW_EXPAND_ANIMATION_MILLIS).start();
            }
        }


    }

    public interface UserReviewCallback {
        void onReplyClicked();

        void onReplyOnReviewClicked(UserReview userReview);
    }
}
