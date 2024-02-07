package com.example.mapd721_a2_nibhamaharjan

import android.annotation.SuppressLint
import android.content.ContentProviderOperation
import android.content.pm.PackageManager
import android.os.Bundle
import android.provider.ContactsContract
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Person
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.example.mapd721_a2_nibhamaharjan.ui.theme.MAPD721A2NibhaMaharjanTheme

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            MAPD721A2NibhaMaharjanTheme {
                Surface(
                    modifier = Modifier.fillMaxSize(),
                    color = MaterialTheme.colorScheme.background
                ) {
                    ContactsScreen()
                }
            }
        }
    }
}

@Composable
fun ContactsScreen() {
    var contactName by remember { mutableStateOf("") }
    var contactNumber by remember { mutableStateOf("") }
    var contacts by remember { mutableStateOf(emptyList<Contact>()) }
    val context = LocalContext.current as ComponentActivity

    // Request permission when the screen is launched
    DisposableEffect(context) {
        requestWriteContactsPermission(context)
        onDispose { /* cleanup */ }
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp)
    ) {
        // UI for adding contacts
        OutlinedTextField(
            value = contactName,
            onValueChange = { contactName = it },
            label = { Text("Contact Name") },
            modifier = Modifier
                .fillMaxWidth()
                .padding(bottom = 8.dp)
        )
        OutlinedTextField(
            value = contactNumber,
            onValueChange = {
                // Filter out non-digit characters and limit to 10 digits
                contactNumber = it.filter { char -> char.isDigit() }.take(10)
            },
            label = { Text("Contact Number") },
            singleLine = true,
            keyboardOptions = KeyboardOptions(
                keyboardType = KeyboardType.Number
            ),
            modifier = Modifier
                .fillMaxWidth()
                .padding(bottom = 16.dp),
            maxLines = 1
        )
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(bottom = 16.dp),
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            // Button to add a contact
            Button(
                onClick = {
                    addContact(contactName, contactNumber, context)
                    contacts = loadContacts(context)
                },
                modifier = Modifier
                    .background(MaterialTheme.colorScheme.primary)
            ) {
                Text("Add Contact", color = MaterialTheme.colorScheme.onPrimary)
            }
            // Button to fetch contacts
            Button(
                onClick = {
                    contacts = loadContacts(context)
                },
                modifier = Modifier
                    .background(MaterialTheme.colorScheme.primary)
            ) {
                Text("Fetch Contacts", color = MaterialTheme.colorScheme.onPrimary)
            }
        }
        // Contacts List
        if (contacts.isEmpty()) {
            Text(text = "No contacts available")
        } else {
            LazyColumn {
                items(contacts) { contact ->
                    ContactItem(contact)
                }
            }
        }
        // About Section
        AboutSection()
    }
}

@Composable
fun ContactItem(contact: Contact) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(8.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Icon(imageVector = Icons.Default.Person, contentDescription = null)
        Spacer(modifier = Modifier.width(8.dp))
        Text(text = "${contact.displayName}")
    }
}

@Composable
fun AboutSection() {
    Spacer(modifier = Modifier.height(40.dp))
    Text(
        text = "Student Info:",
        style = MaterialTheme.typography.bodyLarge
    )
    Spacer(modifier = Modifier.height(8.dp))
    Text("Student Name: Nibha Maharjan")
    Text("Student ID: 301282952")
}

data class Contact(val displayName: String)

@SuppressLint("Range")
fun loadContacts(context: ComponentActivity): List<Contact> {
    val contacts = mutableListOf<Contact>()

    context.contentResolver.query(
        ContactsContract.Contacts.CONTENT_URI,
        arrayOf(ContactsContract.Contacts.DISPLAY_NAME_PRIMARY),
        null,
        null,
        ContactsContract.Contacts.DISPLAY_NAME_PRIMARY
    )?.use { cursor ->
        if (cursor.moveToFirst()) {
            do {
                val displayName =
                    cursor.getString(cursor.getColumnIndex(ContactsContract.Contacts.DISPLAY_NAME_PRIMARY))
                contacts.add(Contact(displayName))
            } while (cursor.moveToNext())
        }
    }

    return contacts
}

fun requestWriteContactsPermission(context: ComponentActivity) {
    if (context.checkSelfPermission(android.Manifest.permission.WRITE_CONTACTS) != PackageManager.PERMISSION_GRANTED) {
        context.requestPermissions(arrayOf(android.Manifest.permission.WRITE_CONTACTS), 1)
    }
}

fun addContact(name: String, number: String, context: ComponentActivity) {
    val ops = ArrayList<ContentProviderOperation>()

    ops.add(
        ContentProviderOperation.newInsert(ContactsContract.RawContacts.CONTENT_URI)
            .withValue(ContactsContract.RawContacts.ACCOUNT_TYPE, null)
            .withValue(ContactsContract.RawContacts.ACCOUNT_NAME, null)
            .build()
    )

    ops.add(
        ContentProviderOperation.newInsert(ContactsContract.Data.CONTENT_URI)
            .withValueBackReference(ContactsContract.Data.RAW_CONTACT_ID, 0)
            .withValue(ContactsContract.Data.MIMETYPE, ContactsContract.CommonDataKinds.StructuredName.CONTENT_ITEM_TYPE)
            .withValue(ContactsContract.CommonDataKinds.StructuredName.DISPLAY_NAME, name)
            .build()
    )

    ops.add(
        ContentProviderOperation.newInsert(ContactsContract.Data.CONTENT_URI)
            .withValueBackReference(ContactsContract.Data.RAW_CONTACT_ID, 0)
            .withValue(ContactsContract.Data.MIMETYPE, ContactsContract.CommonDataKinds.Phone.CONTENT_ITEM_TYPE)
            .withValue(ContactsContract.CommonDataKinds.Phone.NUMBER, number)
            .withValue(ContactsContract.CommonDataKinds.Phone.TYPE, ContactsContract.CommonDataKinds.Phone.TYPE_MOBILE)
            .build()
    )

    try {
        context.contentResolver.applyBatch(ContactsContract.AUTHORITY, ops)
    } catch (e: Exception) {
        e.printStackTrace()
    }
}

@Preview(showBackground = true)
@Composable
fun ContactsScreenPreview() {
    MAPD721A2NibhaMaharjanTheme {
        ContactsScreen()
    }
}