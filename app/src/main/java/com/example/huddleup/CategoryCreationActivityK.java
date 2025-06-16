package com.example.huddleup;

import android.app.Activity;
import android.os.Bundle;
import android.util.Log;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

public class CategoryCreationActivityK extends AppCompatActivity {

    private EditText etCategoryName;
    private RecyclerView rvEventSelectionList;
    private Button btnSaveCategory;

    private DatabaseReference mDatabase;
    private List<EventK> allEvents = new ArrayList<>();
    private EventSelectionAdapterK eventSelectionAdapter;

    private String categoryId = null;
    private CategoryK currentCategory;

    private static final String TAG = "CategoryCreationActivityK";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_category_creation);

        mDatabase = FirebaseDatabase.getInstance().getReference();

        etCategoryName = findViewById(R.id.etCategoryName);
        rvEventSelectionList = findViewById(R.id.rvEventSelectionList);
        btnSaveCategory = findViewById(R.id.btnSaveCategory);

        rvEventSelectionList.setLayoutManager(new LinearLayoutManager(this));

        if (getIntent().hasExtra("categoryId")) {
            categoryId = getIntent().getStringExtra("categoryId");
            loadCategoryData(categoryId);
        }

        loadAllEvents();

        btnSaveCategory.setOnClickListener(v -> saveCategory());
    }

    private void loadCategoryData(String id) {
        mDatabase.child("categories").child(id).addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                currentCategory = dataSnapshot.getValue(CategoryK.class);
                if (currentCategory != null) {
                    etCategoryName.setText(currentCategory.getName());
                    Set<String> selectedIds = (currentCategory.getEvents() != null) ?
                            currentCategory.getEvents().keySet() : new HashSet<>();
                    eventSelectionAdapter = new EventSelectionAdapterK(allEvents, selectedIds);
                    rvEventSelectionList.setAdapter(eventSelectionAdapter);
                } else {
                    Toast.makeText(CategoryCreationActivityK.this, "Category not found.", Toast.LENGTH_SHORT).show();
                    finish();
                }
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {
                Log.e(TAG, "Error loading category: " + databaseError.getMessage());
                Toast.makeText(CategoryCreationActivityK.this, "Failed to load category.", Toast.LENGTH_SHORT).show();
                finish();
            }
        });
    }

    private void loadAllEvents() {
        mDatabase.child("events").addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                allEvents.clear();
                for (DataSnapshot postSnapshot : dataSnapshot.getChildren()) {
                    EventK event = postSnapshot.getValue(EventK.class);
                    if (event != null) {
                        allEvents.add(event);
                    }
                }
                if (eventSelectionAdapter == null) {
                    eventSelectionAdapter = new EventSelectionAdapterK(allEvents, new HashSet<>());
                    rvEventSelectionList.setAdapter(eventSelectionAdapter);
                } else {
                    Set<String> previouslySelectedIds = eventSelectionAdapter.getSelectedEventIds();
                    eventSelectionAdapter = new EventSelectionAdapterK(allEvents, previouslySelectedIds);
                    rvEventSelectionList.setAdapter(eventSelectionAdapter);
                }
                eventSelectionAdapter.notifyDataSetChanged();
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {
                Log.e(TAG, "Error loading events: " + databaseError.getMessage());
                Toast.makeText(CategoryCreationActivityK.this, "Failed to load event list.", Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void saveCategory() {
        String categoryName = etCategoryName.getText().toString().trim();
        if (categoryName.isEmpty()) {
            Toast.makeText(this, "Category name cannot be empty.", Toast.LENGTH_SHORT).show();
            return;
        }

        Set<String> selectedEvents = eventSelectionAdapter.getSelectedEventIds();

        if (categoryId == null) {
            categoryId = mDatabase.child("categories").push().getKey();
            currentCategory = new CategoryK(categoryId, categoryName);
        } else {
            if (currentCategory == null) {
                Toast.makeText(this, "Error: Category not loaded. Please try again.", Toast.LENGTH_SHORT).show();
                return;
            }
            currentCategory.setName(categoryName);
            if (currentCategory.getEvents() != null) {
                currentCategory.getEvents().clear();
            } else {
                currentCategory.setEvents(new HashMap<>());
            }
        }

        for (String eventId : selectedEvents) {
            currentCategory.addEvent(eventId);
        }

        if (categoryId != null) {
            mDatabase.child("categories").child(categoryId).setValue(currentCategory)
                    .addOnSuccessListener(aVoid -> {
                        Toast.makeText(CategoryCreationActivityK.this, "Category saved successfully!", Toast.LENGTH_SHORT).show();
                        setResult(Activity.RESULT_OK);
                        finish();
                    })
                    .addOnFailureListener(e -> {
                        Toast.makeText(CategoryCreationActivityK.this, "Failed to save category: " + e.getMessage(), Toast.LENGTH_LONG).show();
                    });
        }
    }
}