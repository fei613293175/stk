package com.zzyihao.stk

import androidx.room.Room
import androidx.test.core.app.ApplicationProvider
import androidx.test.ext.junit.runners.AndroidJUnit4
import com.zzyihao.stk.data.project.ProjectCacheDatabase
import com.zzyihao.stk.data.project.ProjectCategoryCacheEntity
import com.zzyihao.stk.data.project.ProjectDetailCacheEntity
import com.zzyihao.stk.data.project.ProjectPageCacheEntity
import kotlinx.coroutines.runBlocking
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
class ProjectCacheDaoTest {
    private lateinit var database: ProjectCacheDatabase

    @Before
    fun createDatabase() {
        database = Room.inMemoryDatabaseBuilder(
            ApplicationProvider.getApplicationContext(),
            ProjectCacheDatabase::class.java,
        ).build()
    }

    @After
    fun closeDatabase() = database.close()

    @Test
    fun allCacheTablesRoundTrip() = runBlocking {
        val dao = database.projectCacheDao()
        dao.savePage(ProjectPageCacheEntity("home", "{\"items\":[]}", 1))
        dao.saveCategories(listOf(ProjectCategoryCacheEntity("service", "服务", 2, 1)))
        dao.saveDetail(ProjectDetailCacheEntity("42", "{\"id\":42}", 1))

        assertEquals("{\"items\":[]}", dao.readPage("home"))
        assertEquals("service", dao.readCategories().single().categoryId)
        assertEquals("{\"id\":42}", dao.readDetail("42"))
    }
}
