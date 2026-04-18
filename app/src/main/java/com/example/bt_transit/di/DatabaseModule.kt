package com.example.bt_transit.di

import android.content.Context
import androidx.room.Room
import com.example.bt_transit.data.local.BTDatabase
import com.example.bt_transit.data.local.dao.RouteDao
import com.example.bt_transit.data.local.dao.ShapeDao
import com.example.bt_transit.data.local.dao.StopDao
import com.example.bt_transit.data.local.dao.StopTimeDao
import com.example.bt_transit.data.local.dao.TripDao
import com.example.bt_transit.data.local.dao.RatingDao
import com.example.bt_transit.data.local.dao.WaypointDao
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object DatabaseModule {

    @Provides
    @Singleton
    fun provideDatabase(@ApplicationContext ctx: Context): BTDatabase =
        Room.databaseBuilder(ctx, BTDatabase::class.java, "bt_transit.db")
            .addMigrations(BTDatabase.MIGRATION_2_3)
            .fallbackToDestructiveMigration()
            .build()

    @Provides fun provideStopDao(db: BTDatabase): StopDao = db.stopDao()
    @Provides fun provideRouteDao(db: BTDatabase): RouteDao = db.routeDao()
    @Provides fun provideTripDao(db: BTDatabase): TripDao = db.tripDao()
    @Provides fun provideStopTimeDao(db: BTDatabase): StopTimeDao = db.stopTimeDao()
    @Provides fun provideShapeDao(db: BTDatabase): ShapeDao = db.shapeDao()
    @Provides fun provideWaypointDao(db: BTDatabase): WaypointDao = db.waypointDao()
    @Provides fun provideRatingDao(db: BTDatabase): RatingDao = db.ratingDao()
}
