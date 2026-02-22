package com.gh4a.fragment;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.gh4a.R;
import com.gh4a.translation.TranslationResult;
import com.gh4a.utils.IntentUtils;
import com.google.android.material.bottomsheet.BottomSheetDialogFragment;

public class TranslationResultBottomSheet extends BottomSheetDialogFragment {
    private static final String ARG_ORIGINAL = "original";
    private static final String ARG_TRANSLATED = "translated";

    public static void show(@NonNull androidx.fragment.app.FragmentManager fragmentManager,
            @NonNull String originalText, @NonNull TranslationResult result) {
        androidx.fragment.app.Fragment existing = fragmentManager.findFragmentByTag("translation_result");
        if (existing instanceof TranslationResultBottomSheet) {
            ((TranslationResultBottomSheet) existing).dismissAllowingStateLoss();
        }

        TranslationResultBottomSheet fragment = new TranslationResultBottomSheet();
        Bundle args = new Bundle();
        args.putString(ARG_ORIGINAL, originalText);
        args.putString(ARG_TRANSLATED, result.translatedText());
        fragment.setArguments(args);
        fragment.show(fragmentManager, "translation_result");
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container,
            @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.translation_result_bottom_sheet, container, false);

        Bundle args = getArguments();
        String originalText = args != null ? args.getString(ARG_ORIGINAL, "") : "";
        String translatedText = args != null ? args.getString(ARG_TRANSLATED, "") : "";

        TextView originalView = view.findViewById(R.id.original_text);
        TextView translatedView = view.findViewById(R.id.translated_text);
        originalView.setText(originalText);
        translatedView.setText(translatedText);

        Button copyButton = view.findViewById(R.id.copy_button);
        copyButton.setOnClickListener(v -> {
            IntentUtils.copyToClipboard(requireContext(),
                    getString(R.string.translation_copy_label), translatedText);
            Toast.makeText(requireContext(), R.string.translation_copy_success,
                    Toast.LENGTH_SHORT).show();
        });

        Button closeButton = view.findViewById(R.id.close_button);
        closeButton.setOnClickListener(v -> dismissAllowingStateLoss());

        return view;
    }
}
