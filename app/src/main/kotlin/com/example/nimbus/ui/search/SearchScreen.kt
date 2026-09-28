package com.example.nimbus.ui.search

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.LocationOn
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.ListItem
import androidx.compose.material3.ListItemDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TextField
import androidx.compose.material3.TextFieldDefaults
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardCapitalization
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.nimbus.R
import com.example.nimbus.domain.model.Place
import com.example.nimbus.ui.components.Eyebrow
import com.example.nimbus.ui.components.body
import com.example.nimbus.ui.components.title
import com.example.nimbus.ui.preview.PreviewData
import com.example.nimbus.ui.theme.NimbusTheme

@Composable
fun SearchRoute(viewModel: SearchViewModel, onBack: () -> Unit) {
    val state by viewModel.uiState.collectAsStateWithLifecycle()
    SearchScreen(
        state = state,
        onQueryChange = viewModel::onQueryChange,
        onSelect = { place -> viewModel.select(place, onSaved = onBack) },
        onRemove = viewModel::remove,
        onRetry = viewModel::retry,
        onBack = onBack,
    )
}

/** Find a city and add it, or prune the ones already saved. Stateless; the view model owns the debounce. */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SearchScreen(
    state: SearchUiState,
    onQueryChange: (String) -> Unit,
    onSelect: (Place) -> Unit,
    onRemove: (Place) -> Unit,
    onRetry: () -> Unit,
    onBack: () -> Unit,
) {
    val focusRequester = remember { FocusRequester() }
    LaunchedEffect(Unit) { focusRequester.requestFocus() }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text(stringResource(R.string.search_title)) },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = stringResource(R.string.action_back))
                    }
                },
            )
        },
    ) { padding ->
        Column(Modifier.padding(padding).fillMaxSize()) {
            SearchField(
                query = state.query,
                onQueryChange = onQueryChange,
                focusRequester = focusRequester,
                modifier = Modifier.padding(horizontal = 20.dp),
            )
            Spacer(Modifier.height(8.dp))
            LazyColumn(contentPadding = PaddingValues(horizontal = 8.dp, vertical = 8.dp)) {
                when (val results = state.results) {
                    SearchResults.Idle -> {
                        if (state.savedPlaces.isEmpty()) {
                            item { Hint(stringResource(R.string.search_idle)) }
                        } else {
                            item { Eyebrow(stringResource(R.string.search_saved), Modifier.padding(horizontal = 16.dp, vertical = 8.dp)) }
                            items(state.savedPlaces, key = { it.id }) { place ->
                                PlaceRow(
                                    place = place,
                                    onClick = { onSelect(place) },
                                    modifier = Modifier.animateItem(),
                                    trailing = {
                                        IconButton(onClick = { onRemove(place) }) {
                                            Icon(
                                                Icons.Default.Delete,
                                                contentDescription = stringResource(R.string.action_remove_place, place.name),
                                            )
                                        }
                                    },
                                )
                            }
                        }
                    }
                    SearchResults.Searching -> {
                        item {
                            Row(
                                Modifier.padding(16.dp),
                                verticalAlignment = Alignment.CenterVertically,
                            ) {
                                CircularProgressIndicator(Modifier.size(20.dp), strokeWidth = 2.dp)
                                Spacer(Modifier.width(12.dp))
                                Text(stringResource(R.string.search_searching), style = MaterialTheme.typography.bodyMedium)
                            }
                        }
                    }
                    is SearchResults.Found -> {
                        item { Eyebrow(stringResource(R.string.search_results), Modifier.padding(horizontal = 16.dp, vertical = 8.dp)) }
                        items(results.places, key = { it.id }) { place ->
                            PlaceRow(place = place, onClick = { onSelect(place) }, modifier = Modifier.animateItem())
                        }
                    }
                    is SearchResults.Empty -> {
                        item { Hint(stringResource(R.string.search_empty, results.query)) }
                    }
                    is SearchResults.Failed -> {
                        item {
                            Column(Modifier.padding(16.dp)) {
                                Text(results.error.title(), style = MaterialTheme.typography.titleMedium)
                                Text(
                                    results.error.body(),
                                    style = MaterialTheme.typography.bodyMedium,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                                )
                                TextButton(onClick = onRetry) { Text(stringResource(R.string.action_retry)) }
                            }
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun SearchField(
    query: String,
    onQueryChange: (String) -> Unit,
    focusRequester: FocusRequester,
    modifier: Modifier = Modifier,
) {
    TextField(
        value = query,
        onValueChange = onQueryChange,
        modifier = modifier.fillMaxWidth().focusRequester(focusRequester),
        placeholder = { Text(stringResource(R.string.search_hint)) },
        leadingIcon = { Icon(Icons.Default.Search, contentDescription = null) },
        trailingIcon = {
            if (query.isNotEmpty()) {
                IconButton(onClick = { onQueryChange("") }) {
                    Icon(Icons.Default.Close, contentDescription = stringResource(R.string.action_clear))
                }
            }
        },
        singleLine = true,
        shape = CircleShape,
        keyboardOptions = KeyboardOptions(
            capitalization = KeyboardCapitalization.Words,
            imeAction = ImeAction.Search,
        ),
        colors = TextFieldDefaults.colors(
            focusedIndicatorColor = Color.Transparent,
            unfocusedIndicatorColor = Color.Transparent,
            disabledIndicatorColor = Color.Transparent,
        ),
    )
}

@Composable
private fun PlaceRow(
    place: Place,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    trailing: (@Composable () -> Unit)? = null,
) {
    ListItem(
        headlineContent = { Text(place.name) },
        supportingContent = if (place.subtitle.isNotBlank()) {
            { Text(place.subtitle) }
        } else {
            null
        },
        leadingContent = {
            Icon(Icons.Default.LocationOn, contentDescription = null, tint = MaterialTheme.colorScheme.primary)
        },
        trailingContent = trailing,
        modifier = modifier.clickable(onClick = onClick),
        colors = ListItemDefaults.colors(containerColor = Color.Transparent),
    )
}

@Composable
private fun Hint(text: String) {
    Text(
        text,
        modifier = Modifier.padding(horizontal = 16.dp, vertical = 12.dp),
        style = MaterialTheme.typography.bodyMedium,
        color = MaterialTheme.colorScheme.onSurfaceVariant,
    )
}

@Preview(showBackground = true)
@Composable
private fun SearchScreenPreview() {
    NimbusTheme(dynamicColor = false) {
        SearchScreen(
            state = SearchUiState(
                query = "Man",
                results = SearchResults.Found(PreviewData.places),
            ),
            onQueryChange = {},
            onSelect = {},
            onRemove = {},
            onRetry = {},
            onBack = {},
        )
    }
}

@Preview(showBackground = true)
@Composable
private fun SearchScreenSavedPreview() {
    NimbusTheme(dynamicColor = false) {
        SearchScreen(
            state = SearchUiState(savedPlaces = PreviewData.places),
            onQueryChange = {},
            onSelect = {},
            onRemove = {},
            onRetry = {},
            onBack = {},
        )
    }
}
