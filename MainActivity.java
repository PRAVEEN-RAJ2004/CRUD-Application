package com.example.sqlitecrud;

import android.app.AlertDialog;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.google.android.material.floatingactionbutton.FloatingActionButton;

import java.util.ArrayList;
import java.util.List;

public class MainActivity extends AppCompatActivity {

    private DatabaseHelper   dbHelper;
    private ListView         listView;
    private TextView         tvEmpty;
    private TextView         tvCount;
    private ArrayAdapter<String> adapter;
    private List<Student>    studentList = new ArrayList<>();
    private List<String>     displayList = new ArrayList<>();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        dbHelper = new DatabaseHelper(this);

        listView = findViewById(R.id.listView);
        tvEmpty  = findViewById(R.id.tvEmpty);
        tvCount  = findViewById(R.id.tvCount);

        FloatingActionButton fab = findViewById(R.id.fab);
        fab.setOnClickListener(v -> showAddDialog());

        // Short tap → Edit | Long tap → Delete
        listView.setOnItemClickListener((parent, view, position, id) ->
                showEditDialog(studentList.get(position)));

        listView.setOnItemLongClickListener((parent, view, position, id) -> {
            showDeleteConfirm(studentList.get(position));
            return true;
        });

        adapter = new ArrayAdapter<>(this, R.layout.item_student, R.id.tvItem, displayList);
        listView.setAdapter(adapter);

        loadStudents();
    }

    // ─────────────────────────────────────────────
    //  Reload list from DB
    // ─────────────────────────────────────────────
    private void loadStudents() {
        studentList = dbHelper.getAllStudents();
        displayList.clear();
        for (Student s : studentList) {
            displayList.add(buildDisplay(s));
        }
        adapter.notifyDataSetChanged();

        int count = studentList.size();
        tvCount.setText("Total Records: " + count);
        tvEmpty.setVisibility(count == 0 ? View.VISIBLE : View.GONE);
        listView.setVisibility(count == 0 ? View.GONE  : View.VISIBLE);
    }

    private String buildDisplay(Student s) {
        return "🎓 " + s.getName()
                + "\n   Age: " + s.getAge()
                + "  •  Course: " + s.getCourse()
                + "\n   ✉ " + s.getEmail();
    }

    // ─────────────────────────────────────────────
    //  ADD dialog (CREATE)
    // ─────────────────────────────────────────────
    private void showAddDialog() {
        View view = LayoutInflater.from(this).inflate(R.layout.dialog_student, null);
        EditText etName   = view.findViewById(R.id.etName);
        EditText etAge    = view.findViewById(R.id.etAge);
        EditText etEmail  = view.findViewById(R.id.etEmail);
        EditText etCourse = view.findViewById(R.id.etCourse);

        new AlertDialog.Builder(this)
                .setTitle("➕  Add Student")
                .setView(view)
                .setPositiveButton("Save", (dialog, which) -> {
                    String name   = etName.getText().toString().trim();
                    String ageStr = etAge.getText().toString().trim();
                    String email  = etEmail.getText().toString().trim();
                    String course = etCourse.getText().toString().trim();

                    if (TextUtils.isEmpty(name)) {
                        Toast.makeText(this, "Name is required", Toast.LENGTH_SHORT).show();
                        return;
                    }
                    int age = TextUtils.isEmpty(ageStr) ? 0 : Integer.parseInt(ageStr);
                    long id = dbHelper.insertStudent(name, age, email, course);
                    if (id != -1) {
                        Toast.makeText(this, "Student added ✅", Toast.LENGTH_SHORT).show();
                        loadStudents();
                    } else {
                        Toast.makeText(this, "Insert failed ❌", Toast.LENGTH_SHORT).show();
                    }
                })
                .setNegativeButton("Cancel", null)
                .show();
    }

    // ─────────────────────────────────────────────
    //  EDIT dialog (UPDATE)
    // ─────────────────────────────────────────────
    private void showEditDialog(Student student) {
        View view = LayoutInflater.from(this).inflate(R.layout.dialog_student, null);
        EditText etName   = view.findViewById(R.id.etName);
        EditText etAge    = view.findViewById(R.id.etAge);
        EditText etEmail  = view.findViewById(R.id.etEmail);
        EditText etCourse = view.findViewById(R.id.etCourse);

        // Pre-fill existing values
        etName.setText(student.getName());
        etAge.setText(String.valueOf(student.getAge()));
        etEmail.setText(student.getEmail());
        etCourse.setText(student.getCourse());

        new AlertDialog.Builder(this)
                .setTitle("✏️  Edit Student")
                .setView(view)
                .setPositiveButton("Update", (dialog, which) -> {
                    String name   = etName.getText().toString().trim();
                    String ageStr = etAge.getText().toString().trim();
                    String email  = etEmail.getText().toString().trim();
                    String course = etCourse.getText().toString().trim();

                    if (TextUtils.isEmpty(name)) {
                        Toast.makeText(this, "Name is required", Toast.LENGTH_SHORT).show();
                        return;
                    }
                    int age = TextUtils.isEmpty(ageStr) ? 0 : Integer.parseInt(ageStr);
                    int rows = dbHelper.updateStudent(student.getId(), name, age, email, course);
                    if (rows > 0) {
                        Toast.makeText(this, "Student updated ✅", Toast.LENGTH_SHORT).show();
                        loadStudents();
                    } else {
                        Toast.makeText(this, "Update failed ❌", Toast.LENGTH_SHORT).show();
                    }
                })
                .setNegativeButton("Cancel", null)
                .show();
    }

    // ─────────────────────────────────────────────
    //  DELETE confirmation
    // ─────────────────────────────────────────────
    private void showDeleteConfirm(Student student) {
        new AlertDialog.Builder(this)
                .setTitle("🗑️  Delete Student")
                .setMessage("Delete \"" + student.getName() + "\"?\nThis cannot be undone.")
                .setPositiveButton("Delete", (dialog, which) -> {
                    dbHelper.deleteStudent(student.getId());
                    Toast.makeText(this, "Student deleted 🗑️", Toast.LENGTH_SHORT).show();
                    loadStudents();
                })
                .setNegativeButton("Cancel", null)
                .show();
    }

    // ─────────────────────────────────────────────
    //  Menu – View All / Delete All
    // ─────────────────────────────────────────────
    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.main_menu, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        if (item.getItemId() == R.id.action_delete_all) {
            if (studentList.isEmpty()) {
                Toast.makeText(this, "No records to delete", Toast.LENGTH_SHORT).show();
                return true;
            }
            new AlertDialog.Builder(this)
                    .setTitle("⚠️  Delete All")
                    .setMessage("Delete ALL " + studentList.size() + " records? This cannot be undone.")
                    .setPositiveButton("Delete All", (d, w) -> {
                        for (Student s : studentList) dbHelper.deleteStudent(s.getId());
                        Toast.makeText(this, "All records deleted", Toast.LENGTH_SHORT).show();
                        loadStudents();
                    })
                    .setNegativeButton("Cancel", null)
                    .show();
            return true;
        }
        return super.onOptionsItemSelected(item);
    }
}
