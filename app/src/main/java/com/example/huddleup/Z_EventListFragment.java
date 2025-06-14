package com.example.huddleup;

import android.content.Intent;
import android.os.Bundle;
import android.view.ContextMenu;
import android.view.LayoutInflater;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.Toast;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import java.util.ArrayList;

public class Z_EventListFragment extends Fragment {

    private RecyclerView eventRecyclerView;
    private Button btnGoToMyEvents;
    private Z_EventAdapterP2 eventAdapter;
    private ArrayList<Z_EventP2> eventList;
    private DatabaseReference databaseReference;
    private int selectedEventPosition = -1;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.z_fragment_event_list, container, false);

        eventRecyclerView = view.findViewById(R.id.eventRecyclerViewFragment);
        FloatingActionButton fabAddEvent = view.findViewById(R.id.fabAddEvent);
        btnGoToMyEvents = view.findViewById(R.id.btnGoToMyEvents);

        eventList = new ArrayList<>();
        eventAdapter = new Z_EventAdapterP2(getContext(), eventList);
        eventRecyclerView.setLayoutManager(new LinearLayoutManager(getContext()));
        eventRecyclerView.setAdapter(eventAdapter);

        databaseReference = FirebaseDatabase.getInstance().getReference("events");
        fetchEvents();

        fabAddEvent.setOnClickListener(v -> {
            if (getActivity() instanceof Z_MainActivity) {
                ((Z_MainActivity) getActivity()).showEventFormFragment(null);
            }
        });

        btnGoToMyEvents.setOnClickListener(v -> {
            Intent intent = new Intent(getActivity(), N_EventKu.class);
            startActivity(intent);
        });

        eventAdapter.setOnItemContextClickListener((itemView, position) -> {
            selectedEventPosition = position;
            itemView.showContextMenu();
        });

        return view;
    }

    private void fetchEvents() {
        databaseReference.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                eventList.clear();
                for (DataSnapshot dataSnapshot : snapshot.getChildren()) {
                    Z_EventP2 event = dataSnapshot.getValue(Z_EventP2.class);
                    if (event != null) eventList.add(event);
                }
                eventAdapter.notifyDataSetChanged();
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                if (getContext() != null) {
                    Toast.makeText(getContext(), "Gagal memuat data", Toast.LENGTH_SHORT).show();
                }
            }
        });
    }

    @Override
    public boolean onContextItemSelected(@NonNull MenuItem item) {
        if (selectedEventPosition < 0 || selectedEventPosition >= eventList.size()) {
            return super.onContextItemSelected(item);
        }
        Z_EventP2 selectedEvent = eventList.get(selectedEventPosition);

        int itemId = item.getItemId();
        if (itemId == R.id.action_edit_event) {
            if (getActivity() instanceof Z_MainActivity) {
                ((Z_MainActivity) getActivity()).showEventFormFragment(selectedEvent);
            }
            return true;
        } else if (itemId == R.id.action_delete_event) {
            if (selectedEvent.getId() != null) {
                databaseReference.child(selectedEvent.getId()).removeValue();
                Toast.makeText(getContext(), "Event dihapus", Toast.LENGTH_SHORT).show();
            }
            return true;
        } else {
            return super.onContextItemSelected(item);
        }
    }
}
