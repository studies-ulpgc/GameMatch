package com.pamn.gamematch.ui.community

import com.pamn.gamematch.data.model.UserProfile
import com.pamn.gamematch.data.repository.CommunityRepository
import com.pamn.gamematch.data.repository.GameRepository
import com.pamn.gamematch.ui.MainDispatcherRule
import io.mockk.coEvery
import io.mockk.mockk
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Rule
import org.junit.Test

class CommunityViewModelTest {

    @get:Rule
    val mainDispatcherRule = MainDispatcherRule()

    private val communityRepository: CommunityRepository = mockk(relaxed = true)
    private val gameRepository: GameRepository = mockk(relaxed = true)
    private lateinit var viewModel: CommunityViewModel

    private val currentUid = "user_me"
    private val user1 = UserProfile(uid = "u1", username = "GamerOne", preferredGenres = listOf("Acción"))
    private val user2 = UserProfile(uid = "u2", username = "ProPlayer", preferredGenres = listOf("Aventura"))
    private val userMe = UserProfile(uid = currentUid, username = "Me", preferredGenres = listOf("Acción"))

    @Before
    fun setUp() {
        coEvery { communityRepository.getCommunityUsers() } returns Result.success(listOf(userMe, user1, user2))
        coEvery { communityRepository.getCurrentUserId() } returns currentUid
        coEvery { communityRepository.getFollowingUserIds() } returns Result.success(listOf("u1"))
        coEvery { gameRepository.getGamesCatalog(any(), any(), any(), any()) } returns Result.success(emptyList())

        viewModel = CommunityViewModel(communityRepository, gameRepository)
    }

    @Test
    fun loadCommunityUsers_filtersOutCurrentUserAndAppliesDefaultFilter() {
        viewModel.loadCommunityUsers()

        val state = viewModel.uiState.value
        assertEquals(2, state.userMatches.size)
        assertEquals(2, state.filteredUserMatches.size)
    }

    @Test
    fun onSearchUserQueryChanged_filtersUserMatchesByUsername() {
        viewModel.loadCommunityUsers()
        viewModel.onSearchUserQueryChanged("gamer")

        val state = viewModel.uiState.value
        assertEquals(1, state.filteredUserMatches.size)
        assertEquals("u1", state.filteredUserMatches.first().uid)
    }

    @Test
    fun setUserFilter_followingOnly_showsOnlyFollowedUsers() {
        viewModel.loadCommunityUsers()
        viewModel.loadFollowingUserIds()

        viewModel.setUserFilter(UserFilter.FOLLOWING_ONLY)

        val state = viewModel.uiState.value
        assertEquals(1, state.filteredUserMatches.size)
        assertEquals("u1", state.filteredUserMatches.first().uid)
    }

    @Test
    fun setUserFilter_andSearchQuery_combinesFilters() {
        viewModel.loadCommunityUsers()
        viewModel.loadFollowingUserIds()

        viewModel.setUserFilter(UserFilter.FOLLOWING_ONLY)
        viewModel.onSearchUserQueryChanged("nonexistent")

        val state = viewModel.uiState.value
        assertTrue(state.filteredUserMatches.isEmpty())
    }

    @Test
    fun applyUserFilters_allFilter_sortsByMatchPercentageDescendingAndUsernameAscending() {
        val userTestMe = UserProfile(uid = currentUid, username = "Me", preferredGenres = listOf("G1", "G2", "G3", "G4"))
        val userLow = UserProfile(uid = "uLow", username = "Zebra", preferredGenres = listOf("G1"))
        val userMidB = UserProfile(uid = "uMidB", username = "beta", preferredGenres = listOf("G1", "G2", "G3"))
        val userMidA = UserProfile(uid = "uMidA", username = "Alpha", preferredGenres = listOf("G1", "G2", "G3"))
        val userHigh = UserProfile(uid = "uHigh", username = "Charlie", preferredGenres = listOf("G1", "G2", "G3", "G4"))

        coEvery { communityRepository.getCommunityUsers() } returns Result.success(listOf(userTestMe, userLow, userMidB, userMidA, userHigh))

        viewModel.loadCommunityUsers()

        val filtered = viewModel.uiState.value.filteredUserMatches
        assertEquals(listOf("uHigh", "uMidA", "uMidB", "uLow"), filtered.map { it.uid })
    }

    @Test
    fun applyUserFilters_sameMatchPercentage_sortsByCommonGenresSizeDescending() {
        val userTestMe = UserProfile(uid = currentUid, username = "Me", preferredGenres = listOf("G1", "G2", "G3", "G4"))
        val userLess = UserProfile(uid = "uLess", username = "Alpha", preferredGenres = listOf("G1", "G2"))
        val userMore = UserProfile(uid = "uMore", username = "Zebra", preferredGenres = listOf("G1", "G2", "G3"))

        coEvery { communityRepository.getCommunityUsers() } returns Result.success(listOf(userTestMe, userLess, userMore))

        viewModel.loadCommunityUsers()

        val filtered = viewModel.uiState.value.filteredUserMatches
        // uMore has 3 common genres (75% match) vs uLess 2 common genres (50% match) -> sorted by match & commonGenres
        assertEquals(listOf("uMore", "uLess"), filtered.map { it.uid })
    }

    @Test
    fun applyUserFilters_sameMatchAndSameCommonGenresCount_sortsByUsernameCaseInsensitiveAscending() {
        val userTestMe = UserProfile(uid = currentUid, username = "Me", preferredGenres = listOf("G1", "G2"))
        val userZebra = UserProfile(uid = "uZebra", username = "zebra", preferredGenres = listOf("G1"))
        val userAlpha = UserProfile(uid = "uAlpha", username = "Alpha", preferredGenres = listOf("G1"))

        coEvery { communityRepository.getCommunityUsers() } returns Result.success(listOf(userTestMe, userZebra, userAlpha))

        viewModel.loadCommunityUsers()

        val filtered = viewModel.uiState.value.filteredUserMatches
        assertEquals(listOf("uAlpha", "uZebra"), filtered.map { it.uid })
    }

    @Test
    fun applyUserFilters_switchFilterToFollowingAndBackToAll_maintainsDescendingOrder() {
        val userTestMe = UserProfile(uid = currentUid, username = "Me", preferredGenres = listOf("G1", "G2", "G3", "G4"))
        val userLow = UserProfile(uid = "uLow", username = "Zebra", preferredGenres = listOf("G1"))
        val userMid = UserProfile(uid = "uMid", username = "Alpha", preferredGenres = listOf("G1", "G2", "G3"))
        val userHigh = UserProfile(uid = "uHigh", username = "Charlie", preferredGenres = listOf("G1", "G2", "G3", "G4"))

        coEvery { communityRepository.getCommunityUsers() } returns Result.success(listOf(userTestMe, userLow, userMid, userHigh))
        coEvery { communityRepository.getFollowingUserIds() } returns Result.success(listOf("uLow", "uHigh"))

        viewModel.loadCommunityUsers()
        viewModel.loadFollowingUserIds()

        // Pasar a FOLLOWING_ONLY
        viewModel.setUserFilter(UserFilter.FOLLOWING_ONLY)
        val followingFiltered = viewModel.uiState.value.filteredUserMatches
        assertEquals(listOf("uHigh", "uLow"), followingFiltered.map { it.uid })

        // Volver a ALL
        viewModel.setUserFilter(UserFilter.ALL)
        val allFiltered = viewModel.uiState.value.filteredUserMatches
        assertEquals(listOf("uHigh", "uMid", "uLow"), allFiltered.map { it.uid })
    }

    @Test
    fun applyUserFilters_withSearchQuery_resultsRemainSortedByMatchPercentageDescending() {
        val userTestMe = UserProfile(uid = currentUid, username = "Me", preferredGenres = listOf("G1", "G2", "G3", "G4"))
        val userLow = UserProfile(uid = "uLow", username = "GamerZebra", preferredGenres = listOf("G1"))
        val userHigh = UserProfile(uid = "uHigh", username = "GamerAlpha", preferredGenres = listOf("G1", "G2", "G3", "G4"))

        coEvery { communityRepository.getCommunityUsers() } returns Result.success(listOf(userTestMe, userLow, userHigh))

        viewModel.loadCommunityUsers()
        viewModel.onSearchUserQueryChanged("gamer")

        val filtered = viewModel.uiState.value.filteredUserMatches
        assertEquals(listOf("uHigh", "uLow"), filtered.map { it.uid })
    }
}
