import android.util.Log
import androidx.arch.core.executor.testing.InstantTaskExecutorRule
import androidx.lifecycle.MutableLiveData
import androidx.paging.*
import androidx.recyclerview.widget.ListUpdateCallback
import com.xstory.storysnap.app.data.remote.response.ListStoryItem
import com.xstory.storysnap.app.data.repository.StoryRepository
import com.xstory.storysnap.app.data.repository.UserRepository
import com.xstory.storysnap.app.util.DataDummy
import com.xstory.storysnap.app.util.getOrAwaitValue
import com.xstory.storysnap.app.view.home.HomeViewModel
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.runTest
import org.junit.Assert
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.mockito.ArgumentMatchers.anyInt
import org.mockito.ArgumentMatchers.anyString
import org.mockito.*
import org.mockito.Mockito.`when` as whenever
import org.mockito.junit.MockitoJUnitRunner
import org.mockito.MockedStatic

@ExperimentalCoroutinesApi
@RunWith(MockitoJUnitRunner::class)
class HomeViewModelTest {

    @get:Rule
    val instantExecutorRule = InstantTaskExecutorRule()

    @get:Rule
    val mainDispatcherRule = MainDispatcherRule()

    @Mock
    private lateinit var storyRepository: StoryRepository

    @Mock
    private lateinit var userRepository: UserRepository

    private lateinit var homeViewModel: HomeViewModel
    private lateinit var mockLog: MockedStatic<Log>

    @Before
    fun setUp() {
        mockLog = Mockito.mockStatic(Log::class.java)
        whenever(Log.isLoggable(anyString(), anyInt())).thenReturn(true)
        homeViewModel = HomeViewModel(userRepository, storyRepository)
    }

    @org.junit.After
    fun tearDown() {
        mockLog.close()
    }

    @Test
    fun `when Get Story Should Not Null and Return Data`() = runTest {
        val dummyToken = "this is token"
        val dummyStories = DataDummy.pagingListStory()
        val data: PagingData<ListStoryItem> = PagingData.from(dummyStories)
        val expectedStories = MutableLiveData<PagingData<ListStoryItem>>()
        expectedStories.value = data

        Mockito.`when`(storyRepository.getStories(dummyToken)).thenReturn(expectedStories)

        val actualStories: PagingData<ListStoryItem> =
            homeViewModel.fetchStories(dummyToken).getOrAwaitValue()

        val differ = AsyncPagingDataDiffer(
            diffCallback = ListStoryItem.DIFF_CALLBACK,
            updateCallback = noopListUpdateCallback,
            workerDispatcher = mainDispatcherRule.dispatcher
        )
        differ.submitData(actualStories)

        Assert.assertNotNull(differ.snapshot())
        Assert.assertEquals(dummyStories.size, differ.snapshot().size)
        Assert.assertEquals(dummyStories.first(), differ.snapshot()[0])
    }

    @Test
    fun `when Get Story Empty Should Return No Data`() = runTest {
        val dummyToken = "this is token"
        val emptyData: PagingData<ListStoryItem> = PagingData.empty()
        val expectedStories = MutableLiveData<PagingData<ListStoryItem>>()
        expectedStories.value = emptyData

        Mockito.`when`(storyRepository.getStories(dummyToken)).thenReturn(expectedStories)

        val actualStories: PagingData<ListStoryItem> =
            homeViewModel.fetchStories(dummyToken).getOrAwaitValue()

        val differ = AsyncPagingDataDiffer(
            diffCallback = ListStoryItem.DIFF_CALLBACK,
            updateCallback = noopListUpdateCallback,
            workerDispatcher = mainDispatcherRule.dispatcher
        )
        differ.submitData(actualStories)

        Assert.assertNotNull(differ.snapshot())
        Assert.assertTrue(differ.snapshot().isEmpty())
        Assert.assertEquals(0, differ.snapshot().size)
    }


}

val noopListUpdateCallback = object : ListUpdateCallback {
    override fun onInserted(position: Int, count: Int) {}
    override fun onRemoved(position: Int, count: Int) {}
    override fun onMoved(fromPosition: Int, toPosition: Int) {}
    override fun onChanged(position: Int, count: Int, payload: Any?) {}
}
