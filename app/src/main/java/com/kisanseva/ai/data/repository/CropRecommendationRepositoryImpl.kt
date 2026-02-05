
@file:OptIn(ExperimentalCoroutinesApi::class)

package com.kisanseva.ai.data.repository

import com.kisanseva.ai.data.local.dao.CropRecommendationDao
import com.kisanseva.ai.data.local.entity.CropRecommendationEntity
import com.kisanseva.ai.data.local.entity.CropRecommendationWithRelations
import com.kisanseva.ai.data.local.entity.InterCropRecommendationEntity
import com.kisanseva.ai.data.local.entity.MonoCropEntity
import com.kisanseva.ai.data.remote.CropRecommendationApi
import com.kisanseva.ai.data.remote.websocket.Actions
import com.kisanseva.ai.data.remote.websocket.WebSocketController
import com.kisanseva.ai.domain.model.CropRecommendationRequestData
import com.kisanseva.ai.domain.model.CropRecommendationResponse
import com.kisanseva.ai.domain.model.CultivatingCrop
import com.kisanseva.ai.domain.model.InterCropRecommendation
import com.kisanseva.ai.domain.model.IntercroppingDetails
import com.kisanseva.ai.domain.model.MonoCrop
import com.kisanseva.ai.domain.model.SelectCropRequestData
import com.kisanseva.ai.domain.model.websocketModels.CropSelectionResponse
import com.kisanseva.ai.domain.repository.CropRecommendationRepository
import com.kisanseva.ai.domain.repository.CultivatingCropRepository
import com.kisanseva.ai.exception.ApiException
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.filter
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.mapNotNull

class CropRecommendationRepositoryImpl(
    private val cropRecommendationApi: CropRecommendationApi,
    private val webSocketController: WebSocketController,
    private val cropRecommendationDao: CropRecommendationDao,
    private val cultivatingCropRepository: CultivatingCropRepository
) : CropRecommendationRepository {

    override suspend fun getCropRecommendationByFarmId(farmId: String): CropRecommendationResponse {
        val localData = cropRecommendationDao.getLatestCropRecommendation(farmId)
        if (localData != null) {
            return mapToDomain(localData)
        }
        val remoteData = cropRecommendationApi.getCropRecommendationByFarmId(farmId)
        cacheRecommendation(remoteData)
        return remoteData
    }

    override suspend fun getCropRecommendationById(recommendationId: String): CropRecommendationResponse {
        val localData = cropRecommendationDao.getCropRecommendationById(recommendationId)
        if (localData != null) {
            return mapToDomain(localData)
        }
        val remoteData = cropRecommendationApi.getCropRecommendationById(recommendationId)
        cacheRecommendation(remoteData)
        return remoteData
    }

    override suspend fun getMonoCropById(monoCropId: String): MonoCrop? {
        val entity = cropRecommendationDao.getMonoCropById(monoCropId)
        return entity?.let { mapToMonoCropDomain(it) }
    }

    override suspend fun getInterCropById(interCropId: String): InterCropRecommendation? {
        val entity = cropRecommendationDao.getInterCropById(interCropId)
        return entity?.let {
            InterCropRecommendation(
                id = it.interCropRecommendation.id,
                rank = it.interCropRecommendation.rank,
                intercropType = it.interCropRecommendation.intercropType,
                noOfCrops = it.interCropRecommendation.noOfCrops,
                arrangement = it.interCropRecommendation.arrangement,
                specificArrangement = it.interCropRecommendation.specificArrangement,
                crops = it.crops.map { monoCrop -> mapToMonoCropDomain(monoCrop) },
                description = it.interCropRecommendation.description,
                benefits = it.interCropRecommendation.benefits
            )
        }
    }

    override suspend fun requestCropRecommendation(farmId: String) {
        webSocketController.sendMessage(
            Actions.CROP_RECOMMENDATION,
            CropRecommendationRequestData(farmId)
        )
    }

    override fun listenToCropRecommendations(): Flow<CropRecommendationResponse> {
        return webSocketController.messages
            .filter { it.action == Actions.CROP_RECOMMENDATION }
            .mapNotNull { it.data as? CropRecommendationResponse }
            .flatMapLatest { recommendation ->
                flow {
                    cacheRecommendation(recommendation)
                    emit(recommendation)
                }
            }
    }

    override suspend fun getLatestCropRecommendation(farmId: String): CropRecommendationResponse? {
        val localData = cropRecommendationDao.getLatestCropRecommendation(farmId)
        return localData?.let { mapToDomain(it) }
    }

    override suspend fun selectCropForCultivation(cropId: String, farmId: String, cropRecommendationResponseId: String) {
        try {
            val selectedCrop: CultivatingCrop = cultivatingCropRepository.getCultivatingCropById(cropId)
            cultivatingCropRepository.saveCultivatingCrop(selectedCrop)
        } catch (e: ApiException) {
            if (e.code != 404) {
                throw e
            }
            try {
                val interCroppingDetails: IntercroppingDetails = cultivatingCropRepository
                    .getIntercroppingDetailsById(cropId)
                cultivatingCropRepository.saveIntercroppingDetails(interCroppingDetails)
            } catch (e: ApiException) {
                if (e.code != 404) {
                    throw e
                }
                webSocketController.sendMessage(
                    Actions.SELECT_CROP_FROM_RECOMMENDATION,
                    SelectCropRequestData(
                        selectedCropId = cropId,
                        farmId = farmId,
                        cropRecommendationResponseId = cropRecommendationResponseId
                    )
                )
                webSocketController.messages
                    .filter { it.action == Actions.SELECT_CROP_FROM_RECOMMENDATION }
                    .mapNotNull { it.data as? CropSelectionResponse }
                    .map { response ->

                    }
                    .first()
            }
        }
    }

    private suspend fun cacheRecommendation(response: CropRecommendationResponse) {
        val recommendationEntity = CropRecommendationEntity(
            id = response.id,
            farmId = response.farmId,
            timestamp = response.timestamp,
            status = response.status
        )

        val allMonoCropsToInsert = mutableListOf<MonoCropEntity>()
        val interCropsToInsert = mutableListOf<InterCropRecommendationEntity>()

        response.monoCrops.forEach { monoCrop ->
            allMonoCropsToInsert.add(mapToMonoCropEntity(monoCrop, response.id, null))
        }

        response.interCrops.forEach { interCrop ->
            interCropsToInsert.add(
                InterCropRecommendationEntity(
                    id = interCrop.id,
                    recommendationId = response.id,
                    rank = interCrop.rank,
                    intercropType = interCrop.intercropType,
                    noOfCrops = interCrop.noOfCrops,
                    arrangement = interCrop.arrangement,
                    specificArrangement = interCrop.specificArrangement,
                    description = interCrop.description,
                    benefits = interCrop.benefits
                )
            )
            interCrop.crops.forEach { monoCrop ->
                allMonoCropsToInsert.add(mapToMonoCropEntity(monoCrop, response.id, interCrop.id))
            }
        }

        cropRecommendationDao.insertCropRecommendationWithRelations(
            recommendationEntity,
            allMonoCropsToInsert,
            interCropsToInsert
        )
    }

    private fun mapToMonoCropEntity(monoCrop: MonoCrop, recommendationId: String, interCropId: String?) = MonoCropEntity(
        id = monoCrop.id,
        recommendationId = recommendationId,
        interCropId = interCropId,
        rank = monoCrop.rank,
        cropName = monoCrop.cropName,
        variety = monoCrop.variety,
        imageUrl = monoCrop.imageUrl,
        suitabilityScore = monoCrop.suitabilityScore,
        confidence = monoCrop.confidence,
        expectedYieldPerAcre = monoCrop.expectedYieldPerAcre,
        sowingWindow = monoCrop.sowingWindow,
        growingPeriodDays = monoCrop.growingPeriodDays,
        financialForecasting = monoCrop.financialForecasting,
        reasons = monoCrop.reasons,
        riskFactors = monoCrop.riskFactors,
        description = monoCrop.description
    )

    private fun mapToDomain(cachedData: CropRecommendationWithRelations): CropRecommendationResponse {
        val interCropDomainModels = cachedData.interCrops.map { interCropWithRelations ->
            InterCropRecommendation(
                id = interCropWithRelations.interCropRecommendation.id,
                rank = interCropWithRelations.interCropRecommendation.rank,
                intercropType = interCropWithRelations.interCropRecommendation.intercropType,
                noOfCrops = interCropWithRelations.interCropRecommendation.noOfCrops,
                arrangement = interCropWithRelations.interCropRecommendation.arrangement,
                specificArrangement = interCropWithRelations.interCropRecommendation.specificArrangement,
                crops = interCropWithRelations.crops.map { mapToMonoCropDomain(it) },
                description = interCropWithRelations.interCropRecommendation.description,
                benefits = interCropWithRelations.interCropRecommendation.benefits
            )
        }

        val monoCropDomainModels = cachedData.allMonoCrops
            .filter { it.interCropId == null }
            .map { mapToMonoCropDomain(it) }

        return CropRecommendationResponse(
            id = cachedData.cropRecommendation.id,
            farmId = cachedData.cropRecommendation.farmId,
            timestamp = cachedData.cropRecommendation.timestamp,
            status = cachedData.cropRecommendation.status,
            monoCrops = monoCropDomainModels,
            interCrops = interCropDomainModels
        )
    }

    private fun mapToMonoCropDomain(entity: MonoCropEntity): MonoCrop {
        return MonoCrop(
            id = entity.id,
            rank = entity.rank,
            cropName = entity.cropName,
            variety = entity.variety,
            imageUrl = entity.imageUrl,
            suitabilityScore = entity.suitabilityScore,
            confidence = entity.confidence,
            expectedYieldPerAcre = entity.expectedYieldPerAcre,
            sowingWindow = entity.sowingWindow,
            growingPeriodDays = entity.growingPeriodDays,
            financialForecasting = entity.financialForecasting,
            reasons = entity.reasons,
            riskFactors = entity.riskFactors,
            description = entity.description
        )
    }
}
