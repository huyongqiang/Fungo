package com.fungo.baseuilib.recycler.multitype;

import android.content.Context;
import androidx.annotation.CheckResult;
import androidx.annotation.NonNull;
import android.view.ViewGroup;

import com.fungo.baseuilib.recycler.BaseRecyclerAdapter;
import com.fungo.baseuilib.recycler.BaseViewHolder;

import java.util.Collections;
import java.util.List;

import static com.fungo.baseuilib.recycler.multitype.Preconditions.checkNotNull;


/**
 * 多种数据类型
 */
public class MultiTypeAdapter extends BaseRecyclerAdapter<Object> {


    private @NonNull
    TypePool typePool;

    public MultiTypeAdapter(Context context) {
        this(context, Collections.emptyList());
    }

    public MultiTypeAdapter(Context context, @NonNull List<?> items) {
        this(context, items, new MultiTypePool());
    }

    public MultiTypeAdapter(Context context, @NonNull List<?> items, @NonNull TypePool pool) {
        super(context);
        checkNotNull(items);
        checkNotNull(pool);
        addAll(items);
        this.typePool = pool;
    }

    public <T> void register(@NonNull Class<? extends T> clazz, @NonNull MultiTypeViewHolder<T, ?> binder) {
        checkNotNull(clazz);
        checkNotNull(binder);
        checkAndRemoveAllTypesIfNeeded(clazz);
        register(clazz, binder, new DefaultLinker<T>());
    }

    <T> void register(
            @NonNull Class<? extends T> clazz,
            @NonNull MultiTypeViewHolder<T, ?> binder,
            @NonNull Linker<T> linker) {
        typePool.register(clazz, binder, linker);
        binder.adapter = this;
    }

    @CheckResult
    public @NonNull
    <T> OneToManyFlow<T> register(@NonNull Class<? extends T> clazz) {
        checkNotNull(clazz);
        checkAndRemoveAllTypesIfNeeded(clazz);
        return new OneToManyBuilder<>(this, clazz);
    }

    public void registerAll(@NonNull final TypePool pool) {
        checkNotNull(pool);
        final int size = pool.size();
        for (int i = 0; i < size; i++) {
            registerWithoutChecking(
                    pool.getClass(i),
                    pool.getItemViewBinder(i),
                    pool.getLinker(i)
            );
        }
    }

    public void setItems(@NonNull List<?> items) {
        checkNotNull(items);
        addAll(items);
    }

    public void setTypePool(@NonNull TypePool typePool) {
        checkNotNull(typePool);
        this.typePool = typePool;
    }


    public @NonNull
    TypePool getTypePool() {
        return typePool;
    }


    @Override
    public int getItemViewType(int position) {
        Object item = getItemData(position);
        return indexInTypesOf(position, item);
    }

    @Override
    @SuppressWarnings("unchecked")
    public final long getItemId(int position) {
        Object item = getItemData(position);
        int itemViewType = getItemViewType(position);
        MultiTypeViewHolder binder = typePool.getItemViewBinder(itemViewType);
        return binder.getItemId(item);
    }

    @Override
    @SuppressWarnings("unchecked")
    public void onViewRecycled(BaseViewHolder holder) {
        if (holder.getItemViewType() < typePool.size())
            getRawBinderByViewHolder(holder).onViewRecycled(holder);
    }

    @Override
    @SuppressWarnings("unchecked")
    public boolean onFailedToRecycleView(BaseViewHolder holder) {
        if (holder.getItemViewType() < typePool.size())
            return getRawBinderByViewHolder(holder).onFailedToRecycleView(holder);
        return false;
    }

    @Override
    @SuppressWarnings("unchecked")
    public void onViewAttachedToWindow(BaseViewHolder holder) {
        if (holder.getItemViewType() < typePool.size())
            getRawBinderByViewHolder(holder).onViewAttachedToWindow(holder);
    }

    @Override
    @SuppressWarnings("unchecked")
    public void onViewDetachedFromWindow(BaseViewHolder holder) {
        if (holder.getItemViewType() < typePool.size())
            getRawBinderByViewHolder(holder).onViewDetachedFromWindow(holder);
    }

    private @NonNull
    MultiTypeViewHolder getRawBinderByViewHolder(@NonNull BaseViewHolder holder) {
        return typePool.getItemViewBinder(holder.getItemViewType());
    }


    int indexInTypesOf(int position, @NonNull Object item) throws BinderNotFoundException {
        int index = typePool.firstIndexOf(item.getClass());
        if (index != -1) {
            @SuppressWarnings("unchecked")
            Linker<Object> linker = (Linker<Object>) typePool.getLinker(index);
            return index + linker.index(position, item);
        }
        throw new BinderNotFoundException(item.getClass());
    }


    private void checkAndRemoveAllTypesIfNeeded(@NonNull Class<?> clazz) {
        if (typePool.unregister(clazz)) {
        }
    }

    @SuppressWarnings("unchecked")
    private void registerWithoutChecking(@NonNull Class clazz, @NonNull MultiTypeViewHolder binder, @NonNull Linker linker) {
        checkAndRemoveAllTypesIfNeeded(clazz);
        register(clazz, binder, linker);
    }

    @Override
    public BaseViewHolder onCreateHolder(ViewGroup parent, int viewType) {
        MultiTypeViewHolder<?, ?> binder = typePool.getItemViewBinder(viewType);
        return binder.onCreateViewHolder(parent);
    }
}
