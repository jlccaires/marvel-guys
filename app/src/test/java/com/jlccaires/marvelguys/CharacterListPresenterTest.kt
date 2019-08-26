package com.jlccaires.marvelguys

import android.net.ConnectivityManager
import androidx.lifecycle.LiveData
import androidx.lifecycle.Observer
import androidx.work.*
import com.jlccaires.marvelguys.data.api.MarvelAPI
import com.jlccaires.marvelguys.data.api.dto.CharacterDataDto
import com.jlccaires.marvelguys.data.api.dto.CharacterDto
import com.jlccaires.marvelguys.data.api.dto.CharacterResponseDto
import com.jlccaires.marvelguys.data.api.dto.ThumbnailDto
import com.jlccaires.marvelguys.data.db.dao.CharacterDao
import com.jlccaires.marvelguys.data.db.entity.CharacterEntity
import com.jlccaires.marvelguys.data.worker.FavoriteSyncWorker
import com.jlccaires.marvelguys.ui.characterList.CharacterListContract
import com.jlccaires.marvelguys.ui.characterList.CharacterListPresenter
import com.jlccaires.marvelguys.ui.vo.CharacterVo
import io.mockk.*
import io.reactivex.Completable
import io.reactivex.Maybe
import io.reactivex.Single
import org.junit.Assert
import org.junit.Before
import org.junit.Test
import java.util.*

class CharacterListPresenterTest {

    private val view: CharacterListContract.View = mockk(relaxed = true)
    private val api: MarvelAPI = mockk(relaxed = true)
    private val cm: ConnectivityManager = mockk(relaxed = true)
    private val workManager: WorkManager = mockk(relaxed = true)
    private val dao: CharacterDao = mockk(relaxed = true)
    private lateinit var presenter: CharacterListPresenter
    private val workerData: LiveData<List<WorkInfo>> = mockk(relaxed = true)
    private lateinit var workerObserver: Observer<List<WorkInfo>>

    @Before
    fun setUp() {
        TestUtils.mockRxSchedulers()

        val observerSlot = slot<Observer<List<WorkInfo>>>()

        every { workManager.getWorkInfosByTagLiveData(any()) } returns workerData
        every { workerData.observe(any(), capture(observerSlot)) } answers { nothing }

        presenter = CharacterListPresenter(view, api, cm, workManager, dao)
        workerObserver = observerSlot.captured
    }

    @Test
    fun testWorkManagerListenerInit() {
        workerObserver.onChanged(
            listOf(
                WorkInfo(
                    UUID.randomUUID(),
                    WorkInfo.State.SUCCEEDED,
                    workDataOf(FavoriteSyncWorker.PARAM_ID to 123),
                    listOf(),
                    1
                )
            )
        )

        verify { view.characterSyncStateChange(123, false) }
    }

    @Test
    fun testListNoConnection() {
        every { cm.activeNetworkInfo.isConnected } returns false

        presenter.listCharacters()

        verifySequence {
            view.clearDataset()
            view.showConnectionError()
        }
    }

    @Test
    fun testListSuccess() {
        val dto = CharacterDto(
            0,
            "Char1",
            "",
            "",
            ThumbnailDto("path", "abc")
        )

        val dataDto = CharacterDataDto(
            0,
            0,
            0,
            0,
            listOf(
                dto,
                dto.copy(id = 1),
                dto.copy(id = 2)
            )
        )
        val response = CharacterResponseDto(
            0,
            "",
            "",
            "",
            "",
            "",
            dataDto
        )
        val entity = CharacterEntity(
            1,
            "",
            "",
            "",
            false
        )
        every { api.listCharacters(0, null) } returns Single.just(response)
        every { cm.activeNetworkInfo.isConnected } returns true
        every { dao.byId(0) } returns Maybe.empty()
        every { dao.byId(1) } returns Maybe.just(entity)
        every { dao.byId(2) } returns Maybe.just(entity.copy(syncing = true))

        presenter.listCharacters()

        val slotResult = slot<List<CharacterVo>>()
        verifySequence {
            view.clearDataset()
            view.showLoading()
            view.hideLoading()
            view.showItems(capture(slotResult))
        }
        val value = slotResult.captured.first()
        Assert.assertEquals(0, value.id)
        Assert.assertEquals("Char1", value.name)
        Assert.assertEquals("path/standard_xlarge.abc", value.thumbUrl)
        Assert.assertFalse(value.isFavorite)
        Assert.assertTrue(slotResult.captured[1].isFavorite)
        Assert.assertFalse(slotResult.captured[1].syncing)
        Assert.assertTrue(slotResult.captured[2].isFavorite)
        Assert.assertTrue(slotResult.captured[2].syncing)
    }

    @Test
    fun testListError() {
        every { api.listCharacters(0, null) } returns Single.error(Exception())
        every { cm.activeNetworkInfo.isConnected } returns true

        presenter.listCharacters()

        verifySequence {
            view.clearDataset()
            view.showLoading()
            view.hideLoading()
            view.clearDataset()
            view.showServerError()
        }
    }

    @Test
    fun testSaveFav() {
        val dto = CharacterDto(
            123,
            "Char1",
            "",
            "",
            ThumbnailDto("path", "abc")
        )

        val dataDto = CharacterDataDto(
            0,
            0,
            0,
            0,
            listOf(dto)
        )
        val response = CharacterResponseDto(
            0,
            "",
            "",
            "",
            "",
            "",
            dataDto
        )
        every { api.getCharacter(123) } returns Single.just(response)
        val entitySlot = slot<CharacterEntity>()
        every { dao.insert(capture(entitySlot)) } returns Completable.complete()
        val workerIdSlot = slot<Int>()
        val mockWorker = mockk<OneTimeWorkRequest>()
        mockkObject(FavoriteSyncWorker)
        every { FavoriteSyncWorker.create(capture(workerIdSlot)) } returns mockWorker

        presenter.handleFavorite(
            CharacterVo(123, "", ""),
            true
        )

        Assert.assertEquals(123, entitySlot.captured.id)
        Assert.assertEquals(123, workerIdSlot.captured)
        verify {
            workManager.enqueueUniqueWork("123", ExistingWorkPolicy.KEEP, mockWorker)
        }
    }

    @Test
    fun testDeleteFav() {
        val entitySlot = slot<Int>()
        every { dao.delete(capture(entitySlot)) } returns Completable.complete()

        presenter.handleFavorite(
            CharacterVo(123, "", ""),
            false
        )

        Assert.assertEquals(123, entitySlot.captured)
    }
}
