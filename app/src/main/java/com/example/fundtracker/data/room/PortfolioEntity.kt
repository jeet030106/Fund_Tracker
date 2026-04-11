package com.example.fundtracker.data.room

import androidx.room.Embedded
import androidx.room.Entity
import androidx.room.Junction
import androidx.room.PrimaryKey
import androidx.room.Relation

@Entity(tableName = "portfolios")
data class PortfolioEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val name: String
)

@Entity(
    tableName = "portfolio_fund_cross_ref",
    primaryKeys = ["portfolioId", "schemeCode"]
)
data class PortfolioFundCrossRef(
    val portfolioId: Long,
    val schemeCode: Int
)

data class PortfolioWithFunds(
    @Embedded val portfolio: PortfolioEntity,
    @Relation(
        parentColumn = "id",
        entityColumn = "schemeCode",
        associateBy = Junction(
            value = PortfolioFundCrossRef::class,
            parentColumn = "portfolioId",
            entityColumn = "schemeCode"
        )
    )
    val funds: List<FundEntity>
)