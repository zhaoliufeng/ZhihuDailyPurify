package io.github.izzyleung.zhihudailypurify.ui.widget;

import android.content.Context;
import android.content.res.Configuration;
import android.graphics.Rect;
import android.graphics.drawable.Drawable;
import android.os.ResultReceiver;
import android.text.*;
import android.text.style.ImageSpan;
import android.util.AttributeSet;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.inputmethod.InputMethodManager;
import android.widget.AutoCompleteTextView;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import io.github.izzyleung.zhihudailypurify.R;

import java.lang.reflect.Method;

public class IzzySearchView extends LinearLayout {
    static final AutoCompleteTextViewReflector HIDDEN_METHOD_INVOKER = new AutoCompleteTextViewReflector();

    private int mMaxWidth;
    private boolean mClearingFocus;

    private SearchAutoComplete mQueryTextView;
    private View mSearchPlate;
    private ImageView mCloseButton;
    private OnCloseListener mOnCloseListener;
    private OnQueryTextListener mOnQueryChangeListener;
    private OnFocusChangeListener mOnQueryTextFocusChangeListener;
    private CharSequence mOldQueryText;
    private CharSequence mQueryHint;
    private Runnable mShowImeRunnable = new Runnable() {
        public void run() {
            InputMethodManager imm = (InputMethodManager)
                    getContext().getSystemService(Context.INPUT_METHOD_SERVICE);

            if (imm != null) {
                HIDDEN_METHOD_INVOKER.showSoftInputUnchecked(imm, IzzySearchView.this, 0);
            }
        }
    };
    private Runnable mUpdateDrawableStateRunnable = new Runnable() {
        public void run() {
            updateFocusedState();
        }
    };

    public IzzySearchView(Context context) {
        this(context, null);
    }

    public IzzySearchView(Context context, AttributeSet attrs) {
        super(context, attrs);

        LayoutInflater inflater = (LayoutInflater) context
                .getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        inflater.inflate(R.layout.izzy_search_view, this, true);

        mQueryTextView = (SearchAutoComplete) findViewById(R.id.search_src_text);
        mQueryTextView.setSearchView(this);

        mSearchPlate = findViewById(R.id.search_plate);
        mCloseButton = (ImageView) findViewById(R.id.search_close_btn);

        OnClickListener mOnClickListener = new OnClickListener() {

            public void onClick(View v) {
                if (v == mCloseButton) {
                    onCloseClicked();
                } else if (v == mQueryTextView) {
                    forceSuggestionQuery();
                }
            }
        };
        mCloseButton.setOnClickListener(mOnClickListener);
        mQueryTextView.setOnClickListener(mOnClickListener);

        TextWatcher mTextWatcher = new TextWatcher() {

            public void beforeTextChanged(CharSequence s, int start, int before, int after) {
            }

            public void onTextChanged(CharSequence s, int start,
                                      int before, int after) {
                IzzySearchView.this.onTextChanged(s);
            }

            public void afterTextChanged(Editable s) {
            }
        };
        mQueryTextView.addTextChangedListener(mTextWatcher);
        TextView.OnEditorActionListener mOnEditorActionListener = new TextView.OnEditorActionListener() {
            public boolean onEditorAction(TextView v, int actionId, KeyEvent event) {
                onSubmitQuery();
                return true;
            }
        };
        mQueryTextView.setOnEditorActionListener(mOnEditorActionListener);

        mQueryTextView.setOnFocusChangeListener(new OnFocusChangeListener() {

            public void onFocusChange(View v, boolean hasFocus) {
                if (mOnQueryTextFocusChangeListener != null) {
                    mOnQueryTextFocusChangeListener.onFocusChange(IzzySearchView.this, hasFocus);
                }
            }
        });

        setFocusable(true);
        updateViewsVisibility();
    }

    static boolean isLandscapeMode(Context context) {
        return context.getResources().getConfiguration().orientation
                == Configuration.ORIENTATION_LANDSCAPE;
    }

    @Override
    public boolean requestFocus(int direction, Rect previouslyFocusedRect) {
        if (mClearingFocus) return false;
        if (!isFocusable()) return false;

        boolean result = mQueryTextView.requestFocus(direction, previouslyFocusedRect);
        if (result) {
            updateViewsVisibility();
        }
        return result;
    }

    @Override
    public void clearFocus() {
        mClearingFocus = true;
        setImeVisibility(false);
        super.clearFocus();
        mQueryTextView.clearFocus();
        mClearingFocus = false;
    }

    @Override
    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        int widthMode = MeasureSpec.getMode(widthMeasureSpec);
        int width = MeasureSpec.getSize(widthMeasureSpec);

        switch (widthMode) {
            case MeasureSpec.AT_MOST:
                // If there is an upper limit, don't exceed maximum width (explicit or implicit)
                if (mMaxWidth > 0) {
                    width = Math.min(mMaxWidth, width);
                } else {
                    width = Math.min(getPreferredWidth(), width);
                }
                break;
            case MeasureSpec.EXACTLY:
                // If an exact width is specified, still don't exceed any specified maximum width
                if (mMaxWidth > 0) {
                    width = Math.min(mMaxWidth, width);
                }
                break;
            case MeasureSpec.UNSPECIFIED:
                // Use maximum width, if specified, else preferred width
                width = mMaxWidth > 0 ? mMaxWidth : getPreferredWidth();
                break;
        }
        widthMode = MeasureSpec.EXACTLY;
        super.onMeasure(MeasureSpec.makeMeasureSpec(width, widthMode), heightMeasureSpec);
    }

    private int getPreferredWidth() {
        return getContext().getResources()
                .getDimensionPixelSize(R.dimen.search_view_preferred_width);
    }

    public void setOnQueryTextListener(OnQueryTextListener listener) {
        mOnQueryChangeListener = listener;
    }

    public void setOnCloseListener(OnCloseListener listener) {
        mOnCloseListener = listener;
    }

    private void onCloseClicked() {
        CharSequence text = mQueryTextView.getText();
        if (TextUtils.isEmpty(text)) {
            if (mOnCloseListener == null || !mOnCloseListener.onClose()) {
                clearFocus();
                updateViewsVisibility();
            }
        } else {
            mQueryTextView.setText("");
            mQueryTextView.requestFocus();
            setImeVisibility(true);
        }

    }

    private void forceSuggestionQuery() {
        HIDDEN_METHOD_INVOKER.doBeforeTextChanged(mQueryTextView);
        HIDDEN_METHOD_INVOKER.doAfterTextChanged(mQueryTextView);
    }

    void onTextFocusChanged() {
        updateViewsVisibility();
        postUpdateFocusedState();
        if (mQueryTextView.hasFocus()) {
            forceSuggestionQuery();
        }
    }

    private void updateViewsVisibility() {
        updateCloseButton();
    }

    private void updateCloseButton() {
        final boolean hasText = !TextUtils.isEmpty(mQueryTextView.getText());
        mCloseButton.setVisibility(hasText ? VISIBLE : GONE);
        mCloseButton.getDrawable().setState(hasText ? ENABLED_STATE_SET : EMPTY_STATE_SET);
    }

    private void postUpdateFocusedState() {
        post(mUpdateDrawableStateRunnable);
    }

    private void updateFocusedState() {
        boolean focused = mQueryTextView.hasFocus();
        mSearchPlate.getBackground().setState(focused ? FOCUSED_STATE_SET : EMPTY_STATE_SET);
        invalidate();
    }

    private void setImeVisibility(final boolean visible) {
        if (visible) {
            post(mShowImeRunnable);
        } else {
            removeCallbacks(mShowImeRunnable);
            InputMethodManager imm = (InputMethodManager)
                    getContext().getSystemService(Context.INPUT_METHOD_SERVICE);

            if (imm != null) {
                imm.hideSoftInputFromWindow(getWindowToken(), 0);
            }
        }
    }

    private void onTextChanged(CharSequence newText) {
        updateCloseButton();
        if (mOnQueryChangeListener != null && !TextUtils.equals(newText, mOldQueryText)) {
            mOnQueryChangeListener.onQueryTextChange(newText.toString());
        }
        mOldQueryText = newText.toString();
    }

    public void setQueryHint(CharSequence hint) {
        mQueryHint = hint;
        updateQueryHint();
    }

    private CharSequence getDecoratedHint(CharSequence hintText) {
        SpannableStringBuilder ssb = new SpannableStringBuilder("   "); // for the icon
        ssb.append(hintText);
        Drawable searchIcon = getContext().getResources().getDrawable(R.drawable.search_view_ic_search);
        int textSize = (int) (mQueryTextView.getTextSize() * 1.25);
        searchIcon.setBounds(0, 0, textSize, textSize);
        ssb.setSpan(new ImageSpan(searchIcon), 1, 2, Spannable.SPAN_EXCLUSIVE_EXCLUSIVE);
        return ssb;
    }

    private void updateQueryHint() {
        if (mQueryHint != null) {
            mQueryTextView.setHint(getDecoratedHint(mQueryHint));
        } else {
            mQueryTextView.setHint(getDecoratedHint(""));
        }
    }

    private void onSubmitQuery() {
        CharSequence query = mQueryTextView.getText();
        if (query != null && TextUtils.getTrimmedLength(query) > 0) {
            if (mOnQueryChangeListener == null
                    || !mOnQueryChangeListener.onQueryTextSubmit(query.toString())) {
                dismissSuggestions();
            }
        }
    }

    private void dismissSuggestions() {
        mQueryTextView.dismissDropDown();
    }

    public void setmOnQueryTextFocusChangeListener(OnFocusChangeListener mOnQueryTextFocusChangeListener) {
        this.mOnQueryTextFocusChangeListener = mOnQueryTextFocusChangeListener;
    }

    public void setmMaxWidth(int mMaxWidth) {
        this.mMaxWidth = mMaxWidth;
    }

    public interface OnCloseListener {
        boolean onClose();
    }

    public interface OnQueryTextListener {
        boolean onQueryTextSubmit(String query);

        boolean onQueryTextChange(String newText);
    }

    public static class SearchAutoComplete extends AutoCompleteTextView {

        private int mThreshold;
        private IzzySearchView mSearchView;

        public SearchAutoComplete(Context context) {
            super(context);
            mThreshold = getThreshold();
        }

        public SearchAutoComplete(Context context, AttributeSet attrs) {
            super(context, attrs);
            mThreshold = getThreshold();
        }

        public SearchAutoComplete(Context context, AttributeSet attrs, int defStyle) {
            super(context, attrs, defStyle);
            mThreshold = getThreshold();
        }

        void setSearchView(IzzySearchView searchView) {
            mSearchView = searchView;
        }

        @Override
        public void setThreshold(int threshold) {
            super.setThreshold(threshold);
            mThreshold = threshold;
        }

        @Override
        protected void replaceText(CharSequence text) {
        }

        @Override
        public void performCompletion() {
        }

        @Override
        public void onWindowFocusChanged(boolean hasWindowFocus) {
            super.onWindowFocusChanged(hasWindowFocus);

            if (hasWindowFocus && mSearchView.hasFocus() && getVisibility() == VISIBLE) {
                InputMethodManager inputManager = (InputMethodManager) getContext()
                        .getSystemService(Context.INPUT_METHOD_SERVICE);
                inputManager.showSoftInput(this, 0);
                if (isLandscapeMode(getContext())) {
                    HIDDEN_METHOD_INVOKER.ensureImeVisible(this, true);
                }
            }
        }

        @Override
        protected void onFocusChanged(boolean focused, int direction, Rect previouslyFocusedRect) {
            super.onFocusChanged(focused, direction, previouslyFocusedRect);
            mSearchView.onTextFocusChanged();
        }

        @Override
        public boolean enoughToFilter() {
            return mThreshold <= 0 || super.enoughToFilter();
        }

        @Override
        public boolean onKeyPreIme(int keyCode, @SuppressWarnings("NullableProblems") KeyEvent event) {
            if (keyCode == KeyEvent.KEYCODE_BACK) {
                if (event.getAction() == KeyEvent.ACTION_DOWN && event.getRepeatCount() == 0) {
                    KeyEvent.DispatcherState state = getKeyDispatcherState();
                    if (state != null) {
                        state.startTracking(event, this);
                    }
                    return true;
                } else if (event.getAction() == KeyEvent.ACTION_UP) {
                    KeyEvent.DispatcherState state = getKeyDispatcherState();
                    if (state != null) {
                        state.handleUpEvent(event);
                    }
                    if (event.isTracking() && !event.isCanceled()) {
                        mSearchView.clearFocus();
                        mSearchView.setImeVisibility(false);
                        return true;
                    }
                }
            }
            return super.onKeyPreIme(keyCode, event);
        }
    }

    private static class AutoCompleteTextViewReflector {
        private Method doBeforeTextChanged, doAfterTextChanged;
        private Method ensureImeVisible;
        private Method showSoftInputUnchecked;

        AutoCompleteTextViewReflector() {
            try {
                doBeforeTextChanged = AutoCompleteTextView.class
                        .getDeclaredMethod("doBeforeTextChanged");
                doBeforeTextChanged.setAccessible(true);
            } catch (NoSuchMethodException e) {
                // Ah well.
            }
            try {
                doAfterTextChanged = AutoCompleteTextView.class
                        .getDeclaredMethod("doAfterTextChanged");
                doAfterTextChanged.setAccessible(true);
            } catch (NoSuchMethodException e) {
                // Ah well.
            }
            try {
                ensureImeVisible = AutoCompleteTextView.class
                        .getMethod("ensureImeVisible", boolean.class);
                ensureImeVisible.setAccessible(true);
            } catch (NoSuchMethodException e) {
                // Ah well.
            }
            try {
                showSoftInputUnchecked = InputMethodManager.class.getMethod(
                        "showSoftInputUnchecked", int.class, ResultReceiver.class);
                showSoftInputUnchecked.setAccessible(true);
            } catch (NoSuchMethodException e) {
                // Ah well.
            }
        }

        void doBeforeTextChanged(AutoCompleteTextView view) {
            if (doBeforeTextChanged != null) {
                try {
                    doBeforeTextChanged.invoke(view);
                } catch (Exception ignored) {
                }
            }
        }

        void doAfterTextChanged(AutoCompleteTextView view) {
            if (doAfterTextChanged != null) {
                try {
                    doAfterTextChanged.invoke(view);
                } catch (Exception ignored) {
                }
            }
        }

        void ensureImeVisible(AutoCompleteTextView view, boolean visible) {
            if (ensureImeVisible != null) {
                try {
                    ensureImeVisible.invoke(view, visible);
                } catch (Exception ignored) {
                }
            }
        }

        void showSoftInputUnchecked(InputMethodManager imm, View view, int flags) {
            if (showSoftInputUnchecked != null) {
                try {
                    showSoftInputUnchecked.invoke(imm, flags, null);
                    return;
                } catch (Exception ignored) {
                }
            }

            imm.showSoftInput(view, flags);
        }
    }
}
