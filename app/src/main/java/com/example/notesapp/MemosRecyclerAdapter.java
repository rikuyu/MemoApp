package com.example.notesapp;

import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.TextView;

import androidx.annotation.NonNull;

import android.text.format.DateFormat;

import androidx.recyclerview.widget.RecyclerView;

import com.firebase.ui.firestore.FirestoreRecyclerAdapter;
import com.firebase.ui.firestore.FirestoreRecyclerOptions;
import com.google.firebase.firestore.DocumentSnapshot;

public class MemosRecyclerAdapter extends FirestoreRecyclerAdapter<Memo, MemosRecyclerAdapter.MemoViewHolder> {

    private static final String TAG = "MemoRecyclerAdapter";
    MemoListener memoListener;

    public MemosRecyclerAdapter(@NonNull FirestoreRecyclerOptions<Memo> options, MemoListener memoListener) {
        super(options);
        this.memoListener = memoListener;
    }

    @NonNull
    @Override
    public MemoViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        LayoutInflater layoutInflater = LayoutInflater.from(parent.getContext());
        View view = layoutInflater.inflate(R.layout.memo_row, parent, false);
        return new MemoViewHolder(view);
    }

    @Override
    protected void onBindViewHolder(@NonNull MemoViewHolder holder, int position, @NonNull Memo memo) {
        holder.memoTextView.setText(memo.getText());
        holder.checkbox.setChecked(memo.isCompleted());
        CharSequence dateCharSeq = DateFormat.format("作成日 M月 d日", memo.getCreated().toDate());
        holder.dateTextView.setText(dateCharSeq);
    }

    class MemoViewHolder extends RecyclerView.ViewHolder {

        TextView memoTextView, dateTextView;
        CheckBox checkbox;

        public MemoViewHolder(@NonNull View itemView) {
            super(itemView);

            memoTextView = itemView.findViewById(R.id.memoTextView);
            dateTextView = itemView.findViewById(R.id.dateTextView);
            checkbox = itemView.findViewById(R.id.checkBox);

            checkbox.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
                @Override
                public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                    DocumentSnapshot snapshot = getSnapshots().getSnapshot(getAdapterPosition());
                    Memo memo = getItem(getAdapterPosition());
                    // ViewBinding時のbooleanと異なる場合だけhandleCheckChanged呼ぶ
                    // （同じ真偽値なら変化しない（更新する必要がない）ので呼ばない）
                    if (memo.isCompleted() != isChecked) {
                        memoListener.handleCheckChanged(isChecked, snapshot);
                    }
                }
            });

            itemView.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    DocumentSnapshot snapshot = getSnapshots().getSnapshot(getAdapterPosition());
                    memoListener.handleEditMemo(snapshot);
                }
            });
        }

        public void deleteMemo() {
            Log.d(TAG, "deleteMemo: " + getAdapterPosition());
            Log.d(TAG, "deleteMemo: " + getSnapshots().getSnapshot(getAdapterPosition()));
            memoListener.handleDeleteItem(getSnapshots().getSnapshot(getAdapterPosition()));
        }
    }

    interface MemoListener {
        public void handleCheckChanged(boolean isChecked, DocumentSnapshot snapshot);
        public void handleEditMemo(DocumentSnapshot snapshot);
        public void handleDeleteItem(DocumentSnapshot snapshot);
    }
}
