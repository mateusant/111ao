package com.example.data

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import androidx.sqlite.db.SupportSQLiteDatabase
import com.example.data.dao.AuditLogDao
import com.example.data.dao.CartographicSurveyDao
import com.example.data.dao.EvidenceDao
import com.example.data.dao.IncidentDao
import com.example.data.dao.LocationPoiDao
import com.example.data.dao.NotificationDao
import com.example.data.dao.OdkFormDao
import com.example.data.dao.UserDao
import com.example.data.model.AuditLogEntity
import com.example.data.model.CartographicSurveyEntity
import com.example.data.model.EvidenceEntity
import com.example.data.model.IncidentEntity
import com.example.data.model.LocationPoiEntity
import com.example.data.model.NotificationEntity
import com.example.data.model.OdkFormEntity
import com.example.data.model.UserEntity
import com.example.gis.CartographicData
import com.example.odk.OdkEngine
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

@Database(
    entities = [
        IncidentEntity::class,
        CartographicSurveyEntity::class,
        EvidenceEntity::class,
        OdkFormEntity::class,
        AuditLogEntity::class,
        UserEntity::class,
        NotificationEntity::class,
        LocationPoiEntity::class
    ],
    version = 3,
    exportSchema = false
)
abstract class AppDatabase : RoomDatabase() {
    abstract fun incidentDao(): IncidentDao
    abstract fun cartographicSurveyDao(): CartographicSurveyDao
    abstract fun evidenceDao(): EvidenceDao
    abstract fun odkFormDao(): OdkFormDao
    abstract fun auditLogDao(): AuditLogDao
    abstract fun userDao(): UserDao
    abstract fun notificationDao(): NotificationDao
    abstract fun locationPoiDao(): LocationPoiDao

    companion object {
        @Volatile
        private var INSTANCE: AppDatabase? = null

        fun getDatabase(context: Context, scope: CoroutineScope): AppDatabase {
            return INSTANCE ?: synchronized(this) {
                val instance = Room.databaseBuilder(
                    context.applicationContext,
                    AppDatabase::class.java,
                    "ocorrencia_remota_db"
                )
                    .fallbackToDestructiveMigration(true)
                    .addCallback(DatabaseCallback(scope))
                    .build()
                INSTANCE = instance
                instance
            }
        }

        private class DatabaseCallback(
            private val scope: CoroutineScope
        ) : RoomDatabase.Callback() {
            override fun onCreate(db: SupportSQLiteDatabase) {
                super.onCreate(db)
                INSTANCE?.let { database ->
                    scope.launch(Dispatchers.IO) {
                        populateDatabase(database)
                    }
                }
            }
        }

        suspend fun populateDatabase(db: AppDatabase) {
            // Inicia com conta particular limpa sem dados demo
            db.userDao().insertUser(OdkEngine.CLEAN_DEFAULT_USER)

            // Seed Official Geographic POIs (Police Stations and Public Services)
            db.locationPoiDao().insertPois(CartographicData.OFFICIAL_POIS)

            // Seed Built-in Official ODK Forms
            val forms = OdkEngine.BUILT_IN_FORMS.map { form ->
                OdkFormEntity(
                    formId = form.id,
                    title = form.title,
                    version = form.version,
                    category = form.category,
                    description = form.description,
                    fieldsJson = form.fields.size.toString(),
                    xmlTemplate = OdkEngine.buildOpenRosaXml(form.id, form.version, data = emptyMap()),
                    isActive = true,
                    submissionCount = 0
                )
            }
            db.odkFormDao().insertForms(forms)

            // Seed Initial Audit Log
            db.auditLogDao().insertLog(
                AuditLogEntity(
                    userBadge = "SYS-000",
                    userName = "Sistema Automático",
                    userRole = "ADMINISTRADOR",
                    action = "CRIACAO",
                    entityType = "BASE_DADOS",
                    entityId = "INIT",
                    details = "Inicialização de esquema PostgreSQL/PostGIS local e carregamento de formulários ODK."
                )
            )
        }
    }
}
