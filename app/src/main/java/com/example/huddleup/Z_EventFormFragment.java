package com.example.huddleup;

import android.net.Uri;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.Toast;
import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import com.bumptech.glide.Glide;
import com.cloudinary.android.MediaManager;
import com.cloudinary.android.callback.ErrorInfo;
import com.cloudinary.android.callback.UploadCallback;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import java.util.HashMap;
import java.util.Map;

public class Z_EventFormFragment extends Fragment {

    private EditText edtEventName, edtEventDate, edtEventTime, edtEventLocation, edtEventAbout;
    private Button btnSave, btnChooseImage;
    private ImageView imgPreview;
    private ProgressBar progressBar;
    private Uri newImageUri;
    private String existingImageUrl;
    private String eventIdToEdit;
    private DatabaseReference databaseReference;

    private static final String CLOUDINARY_CLOUD_NAME = "dogwmbaw4";
    private static final String CLOUDINARY_API_KEY = "492621155953182";
    private static final String CLOUDINARY_API_SECRET = "CySQXQNV7UKNv8pCkURttqO8XPo";
    private static final String CLOUDINARY_UPLOAD_PRESET = "HuddleUp";

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.z_fragment_event_form, container, false);

        configCloudinary();
        databaseReference = FirebaseDatabase.getInstance().getReference("events");

        edtEventName = view.findViewById(R.id.edtEventName);
        edtEventDate = view.findViewById(R.id.edtEventDate);
        edtEventTime = view.findViewById(R.id.edtEventTime);
        edtEventLocation = view.findViewById(R.id.edtEventLocation);
        edtEventAbout = view.findViewById(R.id.edtEventAbout);
        btnSave = view.findViewById(R.id.btnSave);
        btnChooseImage = view.findViewById(R.id.btnChooseImage);
        imgPreview = view.findViewById(R.id.imgPreview);
        progressBar = view.findViewById(R.id.progressBar);

        Bundle args = getArguments();
        if (args != null) {
            getActivity().setTitle("Edit Event");
            eventIdToEdit = args.getString("EVENT_ID");
            edtEventName.setText(args.getString("EVENT_NAME"));
            edtEventDate.setText(args.getString("EVENT_DATE"));
            edtEventTime.setText(args.getString("EVENT_TIME"));
            edtEventLocation.setText(args.getString("EVENT_LOCATION"));
            edtEventAbout.setText(args.getString("EVENT_ABOUT"));
            existingImageUrl = args.getString("EVENT_IMAGE_URL");
            if (getContext() != null) {
                Glide.with(getContext()).load(existingImageUrl).into(imgPreview);
            }
        } else {
            getActivity().setTitle("Tambah Event Baru");
        }

        ActivityResultLauncher<String> pickImageLauncher = registerForActivityResult(
                new ActivityResultContracts.GetContent(),
                uri -> {
                    if (uri != null) {
                        newImageUri = uri;
                        imgPreview.setImageURI(newImageUri);
                    }
                });

        btnChooseImage.setOnClickListener(v -> pickImageLauncher.launch("image/*"));
        btnSave.setOnClickListener(v -> handleSaveEvent());

        return view;
    }

    private void configCloudinary() {
        if (getContext() == null) return;
        Map<String, String> config = new HashMap<>();
        config.put("cloud_name", CLOUDINARY_CLOUD_NAME);
        config.put("api_key", CLOUDINARY_API_KEY);
        config.put("api_secret", CLOUDINARY_API_SECRET);
        try {
            MediaManager.init(getContext(), config);
        } catch (IllegalStateException e) {
        }
    }

    private void handleSaveEvent() {
        if (newImageUri != null) {
            uploadImageToCloudinary();
        } else if (eventIdToEdit != null) {
            saveDataToFirebase(existingImageUrl);
        } else {
            Toast.makeText(getContext(), "Pilih poster dulu", Toast.LENGTH_SHORT).show();
        }
    }

    private void uploadImageToCloudinary() {
        if (getContext() == null) return;

        progressBar.setVisibility(View.VISIBLE);
        setFormEnabled(false);

        MediaManager.get().upload(newImageUri)
                .option("upload_preset", CLOUDINARY_UPLOAD_PRESET)
                .callback(new UploadCallback() {
                    @Override
                    public void onStart(String requestId) {}

                    @Override
                    public void onProgress(String requestId, long bytes, long totalBytes) {}

                    @Override
                    public void onSuccess(String requestId, Map resultData) {
                        String imageUrl = (String) resultData.get("secure_url");
                        saveDataToFirebase(imageUrl);
                    }

                    @Override
                    public void onError(String requestId, ErrorInfo error) {
                        if (getActivity() != null) {
                            getActivity().runOnUiThread(() -> {
                                progressBar.setVisibility(View.GONE);
                                setFormEnabled(true);
                                Toast.makeText(getContext(), "Gagal upload: " + error.getDescription(), Toast.LENGTH_LONG).show();
                            });
                        }
                    }

                    @Override
                    public void onReschedule(String requestId, ErrorInfo error) {}
                }).dispatch();
    }

    private void saveDataToFirebase(String imageUrl) {
        if (getActivity() == null) return;

        String id = (eventIdToEdit != null) ? eventIdToEdit : databaseReference.push().getKey();
        String name = edtEventName.getText().toString().trim();
        String date = edtEventDate.getText().toString().trim();
        String time = edtEventTime.getText().toString().trim();
        String location = edtEventLocation.getText().toString().trim();
        String about = edtEventAbout.getText().toString().trim();

        if (name.isEmpty() || id == null) {
            Toast.makeText(getContext(), "Nama tidak boleh kosong", Toast.LENGTH_SHORT).show();
            setFormEnabled(true);
            progressBar.setVisibility(View.GONE);
            return;
        }

        Z_EventP2 event = new Z_EventP2(id, name, imageUrl, date, time, location, about);

        databaseReference.child(id).setValue(event)
                .addOnSuccessListener(aVoid -> {
                    Toast.makeText(getContext(), "Event berhasil disimpan", Toast.LENGTH_SHORT).show();
                    if (getActivity() instanceof Z_MainActivity) {
                        ((Z_MainActivity) getActivity()).showEventListFragment();
                    }
                })
                .addOnFailureListener(e -> {
                    Toast.makeText(getContext(), "Gagal menyimpan data ke database", Toast.LENGTH_SHORT).show();
                    setFormEnabled(true);
                    progressBar.setVisibility(View.GONE);
                });
    }

    private void setFormEnabled(boolean isEnabled) {
        edtEventName.setEnabled(isEnabled);
        edtEventDate.setEnabled(isEnabled);
        edtEventTime.setEnabled(isEnabled);
        edtEventLocation.setEnabled(isEnabled);
        edtEventAbout.setEnabled(isEnabled);
        btnChooseImage.setEnabled(isEnabled);
        btnSave.setEnabled(isEnabled);
    }
}