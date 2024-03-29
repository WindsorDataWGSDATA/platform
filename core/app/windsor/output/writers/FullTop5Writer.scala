package windsor.output.writers

import model.mapping.FullOutputDilutionMappingComponent
import model.mapping.FullOutputGuidelinesMappingComponent
import mapping.DocSrcMappingComponent
import windsor.output.writers.generic.FullTop5WithTtdcMappingComponent
import windsor.output.writers.generic.Top5Writer
import windsor.output.writers.generic.StandardDocSrcMapping

object FullTop5Writer extends Top5Writer
  with StandardDocSrcMapping
  with FullOutputDilutionMappingComponent
  with FullTop5WithTtdcMappingComponent
  with FullOutputGuidelinesMappingComponent {

  override val fileName = "EmptyFullOutputTemplate.xls"
}
