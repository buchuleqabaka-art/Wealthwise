package com.example.wealthwise.ui.transactions

import android.app.DatePickerDialog
import android.content.pm.PackageManager
import android.graphics.Color
import android.net.Uri
import android.os.Bundle
import android.os.Environment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ArrayAdapter
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import androidx.core.content.ContextCompat
import androidx.core.content.FileProvider
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.navigation.fragment.findNavController
import com.example.wealthwise.R
import com.example.wealthwise.database.entities.AppTransaction
import com.example.wealthwise.database.entities.Category
import com.example.wealthwise.databinding.FragmentAddTransactionBinding
import com.example.wealthwise.viewmodel.TransactionViewModel
import java.io.File
import java.text.SimpleDateFormat
import java.util.*

class AddTransactionFragment : Fragment() {

    private var _binding: FragmentAddTransactionBinding? = null
    private val binding get() = _binding!!
    private val viewModel: TransactionViewModel by viewModels()

    // Holds the URI of the photo being taken by the camera
    private var photoUri: Uri? = null
    // Holds the final path string saved to the database
    private var savedPhotoPath: String? = null
    // Holds the loaded category list
    private var categories: List<Category> = emptyList()
    // Currently selected date
    private var selectedDate: String = ""

    //  CAMERA: takes a photo and saves it to a temp file
    private val takePictureLauncher = registerForActivityResult(
        ActivityResultContracts.TakePicture()
    ) { success ->
        if (success && photoUri != null) {
            // Photo was taken successfully — show preview
            savedPhotoPath = photoUri!!.path
            binding.ivReceiptPreview.setImageURI(photoUri)
            binding.ivReceiptPreview.visibility = View.VISIBLE
            Toast.makeText(requireContext(), "Receipt photo saved!", Toast.LENGTH_SHORT).show()
        } else {
            Toast.makeText(requireContext(), "Camera cancelled.", Toast.LENGTH_SHORT).show()
        }
    }

    //  CAMERA PERMISSION
    private val requestCameraPermission = registerForActivityResult(
        ActivityResultContracts.RequestPermission()
    ) { granted ->
        if (granted) {
            openCamera()
        } else {
            Toast.makeText(requireContext(),
                "Camera permission is required to take receipt photos.",
                Toast.LENGTH_LONG).show()
        }
    }

    //  GALLERY: picks an existing image
    private val pickImageLauncher = registerForActivityResult(
        ActivityResultContracts.GetContent()
    ) { uri ->
        if (uri != null) {
            savedPhotoPath = uri.toString()
            binding.ivReceiptPreview.setImageURI(uri)
            binding.ivReceiptPreview.visibility = View.VISIBLE
            Toast.makeText(requireContext(), "Receipt photo selected!", Toast.LENGTH_SHORT).show()
        }
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentAddTransactionBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        // Set today as the default date
        val today = SimpleDateFormat("dd/MM/yyyy", Locale.getDefault()).format(Date())
        selectedDate = today
        binding.etDate.setText(today)

        setupObservers()
        setupListeners()
        updateTypeToggleUI(binding.rbExpense.id)
    }

    private fun setupObservers() {
        // Observe categories from ViewModel (correctly filtered by type and user)
        viewModel.filteredCategories.observe(viewLifecycleOwner) { catList ->
            categories = catList ?: emptyList()
            refreshCategorySpinner(categories)
        }
    }

    private fun refreshCategorySpinner(catList: List<Category>) {
        val names = catList.map { it.name }
        val adapter = ArrayAdapter(
            requireContext(),
            android.R.layout.simple_spinner_item,
            names
        )
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
        binding.spinnerCategory.adapter = adapter
    }

    private fun setupListeners() {

        binding.tvBack.setOnClickListener {
            findNavController().popBackStack()
        }

        // EXPENSE / INCOME TOGGLE
        binding.rgTransactionType.setOnCheckedChangeListener { _, checkedId ->
            updateTypeToggleUI(checkedId)
            val type = if (checkedId == binding.rbExpense.id) "EXPENSE" else "INCOME"
            viewModel.setCategoryTypeFilter(type)
        }

        //  DATE PICKER
        binding.etDate.setOnClickListener { showDatePicker() }
        binding.llDatePicker.setOnClickListener { showDatePicker() }

        //  CAMERA BUTTON
        binding.btnCamera.setOnClickListener {
            when {
                ContextCompat.checkSelfPermission(
                    requireContext(),
                    android.Manifest.permission.CAMERA
                ) == PackageManager.PERMISSION_GRANTED -> openCamera()

                else -> requestCameraPermission.launch(android.Manifest.permission.CAMERA)
            }
        }

        //  GALLERY BUTTON
        binding.btnGallery.setOnClickListener {
            pickImageLauncher.launch("image/*")
        }

        //  SAVE BUTTON
        binding.btnSave.setOnClickListener {
            saveTransaction()
        }
    }

    private fun updateTypeToggleUI(checkedId: Int) {
        if (checkedId == binding.rbExpense.id) {
            binding.rbExpense.setTextColor(Color.WHITE)
            binding.rbExpense.setBackgroundResource(R.drawable.btn_blue_pill)
            binding.rbIncome.setTextColor(Color.parseColor("#B0C4DE"))
            binding.rbIncome.setBackgroundColor(Color.TRANSPARENT)
        } else {
            binding.rbIncome.setTextColor(Color.WHITE)
            binding.rbIncome.setBackgroundResource(R.drawable.btn_blue_pill)
            binding.rbExpense.setTextColor(Color.parseColor("#B0C4DE"))
            binding.rbExpense.setBackgroundColor(Color.TRANSPARENT)
        }
    }

    private fun openCamera() {
        try {
            val photoFile = createImageFile()
            photoUri = FileProvider.getUriForFile(
                requireContext(),
                "${requireContext().packageName}.provider",
                photoFile
            )
            takePictureLauncher.launch(photoUri)
        } catch (e: Exception) {
            Toast.makeText(requireContext(), "Could not open camera: ${e.message}", Toast.LENGTH_LONG).show()
        }
    }

    private fun createImageFile(): File {
        val timeStamp = SimpleDateFormat("yyyyMMdd_HHmmss", Locale.getDefault()).format(Date())
        val storageDir = requireContext().getExternalFilesDir(Environment.DIRECTORY_PICTURES)
        return File.createTempFile(
            "RECEIPT_${timeStamp}_",
            ".jpg",
            storageDir
        ).also { savedPhotoPath = it.absolutePath }
    }

    private fun showDatePicker() {
        val calendar = Calendar.getInstance()
        DatePickerDialog(
            requireContext(),
            { _, year, month, day ->
                selectedDate = "%02d/%02d/%04d".format(day, month + 1, year)
                binding.etDate.setText(selectedDate)
            },
            calendar.get(Calendar.YEAR),
            calendar.get(Calendar.MONTH),
            calendar.get(Calendar.DAY_OF_MONTH)
        ).show()
    }

    private fun saveTransaction() {
        val amountText = binding.etAmount.text.toString().trim()
        val description = binding.etDescription.text.toString().trim()
        val type = if (binding.rbExpense.isChecked) "EXPENSE" else "INCOME"

        if (amountText.isEmpty()) {
            binding.etAmount.error = "Please enter an amount"
            binding.etAmount.requestFocus()
            return
        }

        val amount = amountText.toDoubleOrNull()
        if (amount == null || amount <= 0) {
            binding.etAmount.error = "Please enter a valid amount greater than 0"
            binding.etAmount.requestFocus()
            return
        }

        if (selectedDate.isEmpty()) {
            Toast.makeText(requireContext(), "Please select a date", Toast.LENGTH_SHORT).show()
            return
        }

        if (categories.isEmpty()) {
            Toast.makeText(requireContext(), "Please add a category first.", Toast.LENGTH_LONG).show()
            return
        }

        val selectedCategoryIndex = binding.spinnerCategory.selectedItemPosition
        if (selectedCategoryIndex < 0 || selectedCategoryIndex >= categories.size) {
            Toast.makeText(requireContext(), "Please select a category", Toast.LENGTH_SHORT).show()
            return
        }

        val categoryId = categories[selectedCategoryIndex].id
        val storedDate = convertDateForStorage(selectedDate)

        val transaction = AppTransaction(
            amount      = amount,
            date        = storedDate,
            categoryId  = categoryId,
            description = description,
            type        = type,
            photoPath   = savedPhotoPath
        )

        viewModel.insert(transaction)

        Toast.makeText(requireContext(), "Transaction saved!", Toast.LENGTH_SHORT).show()
        findNavController().popBackStack()
    }

    private fun convertDateForStorage(displayDate: String): String {
        return try {
            val inputFmt  = SimpleDateFormat("dd/MM/yyyy", Locale.getDefault())
            val outputFmt = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault())
            val date = inputFmt.parse(displayDate) ?: Date()
            outputFmt.format(date)
        } catch (e: Exception) {
            SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()).format(Date())
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
