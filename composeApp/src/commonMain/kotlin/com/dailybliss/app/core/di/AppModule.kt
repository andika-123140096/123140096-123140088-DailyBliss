package com.dailybliss.app.core.di

import com.dailybliss.app.core.network.HttpClientFactory
import com.dailybliss.app.core.util.BackgroundAIProcessor
import com.dailybliss.app.core.util.DatabaseDriverFactory
import com.dailybliss.app.data.local.BlissDatabase
import com.dailybliss.app.data.local.datastore.DataStoreFactory
import com.dailybliss.app.data.local.datastore.UserPreferences
import com.dailybliss.app.data.local.datastore.create
import com.dailybliss.app.data.remote.api.GeminiService
import com.dailybliss.app.data.repository.AIRepositoryImpl
import com.dailybliss.app.data.repository.MomentRepositoryImpl
import com.dailybliss.app.domain.repository.AIRepository
import com.dailybliss.app.domain.repository.MomentRepository
import com.dailybliss.app.domain.usecase.*
import com.dailybliss.app.presentation.screens.addnote.CreateMomentViewModel
import com.dailybliss.app.presentation.screens.ai.AIAssistantViewModel
import com.dailybliss.app.presentation.screens.calendar.CalendarViewModel
import com.dailybliss.app.presentation.screens.calendar.DailyMomentsViewModel
import com.dailybliss.app.presentation.screens.detail.MomentDetailViewModel
import com.dailybliss.app.presentation.screens.home.HomeViewModel
import com.dailybliss.app.presentation.screens.home.JournalViewModel
import com.dailybliss.app.presentation.screens.settings.SettingsViewModel
import com.dailybliss.app.presentation.util.FileStorage
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import org.koin.core.context.startKoin
import org.koin.core.module.Module
import org.koin.core.module.dsl.singleOf
import org.koin.core.module.dsl.viewModel
import org.koin.core.module.dsl.viewModelOf
import org.koin.dsl.KoinAppDeclaration
import org.koin.dsl.bind
import org.koin.dsl.module

// ==================== CORE MODULE ====================

val coreModule = module {
    single { CoroutineScope(SupervisorJob() + Dispatchers.Default) }
    single { FileStorage(get()) }
    singleOf(::BackgroundAIProcessor)
}

// ==================== NETWORK MODULE ====================

val networkModule = module {
    single { HttpClientFactory.create(enableLogging = true) }
    singleOf(::GeminiService)
}

// ==================== DATABASE MODULE ====================

val databaseModule = module {
    single {
        val driverFactory: DatabaseDriverFactory = get()
        BlissDatabase(driverFactory.createDriver())
    }
}

// ==================== PREFERENCES MODULE ====================

val preferencesModule = module {
    single { get<DataStoreFactory>().create() }
    single { UserPreferences(get()) }
}

// ==================== REPOSITORY MODULE ====================

val repositoryModule = module {
    singleOf(::MomentRepositoryImpl) bind MomentRepository::class
    single { AIRepositoryImpl(get(), get(), get()) } bind AIRepository::class
}

// ==================== USE CASE MODULE ====================

val useCaseModule = module {
    singleOf(::GetAllMomentsUseCase)
    singleOf(::SearchMomentsUseCase)
    singleOf(::SaveMomentUseCase)
    singleOf(::DeleteMomentUseCase)
    singleOf(::GetMomentByIdUseCase)
    singleOf(::GetMomentsFromSameDayUseCase)
    singleOf(::GetMomentsForDateUseCase)
    singleOf(::GetMomentsByDateRangeUseCase)
}

// ==================== VIEWMODEL MODULE ====================

val viewModelModule = module {
    viewModelOf(::HomeViewModel)
    viewModelOf(::JournalViewModel)
    viewModelOf(::CalendarViewModel)
    viewModel { parameters -> DailyMomentsViewModel(dateStr = parameters.get(), get()) }
    viewModelOf(::CreateMomentViewModel)
    viewModelOf(::MomentDetailViewModel)
    viewModelOf(::AIAssistantViewModel)
    viewModelOf(::SettingsViewModel)
}

// ==================== SHARED MODULES ====================

val sharedModules = listOf(
    coreModule,
    networkModule,
    databaseModule,
    preferencesModule,
    repositoryModule,
    useCaseModule,
    viewModelModule,
)

// ==================== INIT FUNCTION ====================

fun initKoin(platformModules: List<Module> = emptyList(), config: KoinAppDeclaration? = null) {
    startKoin {
        config?.invoke(this)
        modules(platformModules + sharedModules)
    }
}
