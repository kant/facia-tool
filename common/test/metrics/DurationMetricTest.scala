package metrics

import com.amazonaws.services.cloudwatch.model.StandardUnit
import org.scalatest.{FlatSpec, Matchers}
import scala.concurrent.{Await, Future}
import scala.concurrent.duration._
import common.ExecutionContexts

class DurationMetricTest extends FlatSpec with Matchers with ExecutionContexts{

  "DurationMetric" should "start off empty" in {
    val durationMetric: DurationMetric = DurationMetric("TestMetric", StandardUnit.Count)

    durationMetric.getAndResetDataPoints should be (List())
  }

  it should "record some metrics" in {
    val durationMetric: DurationMetric = DurationMetric("TestMetric", StandardUnit.Count)

    durationMetric.recordDuration(1000)
    durationMetric.recordDuration(1000)
    durationMetric.recordDuration(1000)

    Await.result(durationMetric.getDataFuture, 10.seconds)

    durationMetric.getDataPoints.length should be (3)
    durationMetric.getAndResetDataPoints.forall(_.value == 1000) should be (true)

    Await.result(durationMetric.getDataFuture, 10.seconds)

    durationMetric.getDataPoints.length should be(0)
  }

  it should "add datapoints to the head of the list" in {
    val durationMetric: DurationMetric = DurationMetric("TestMetric", StandardUnit.Count)

    val metricOne = DurationDataPoint(1000, None)
    val metricTwo = DurationDataPoint(1000, None)
    val metricThree = DurationDataPoint(1000, None)
    val metricFour = DurationDataPoint(1000, None)
    val allMetrics = List(metricOne, metricTwo, metricThree, metricFour)

    durationMetric.recordDuration(1000)
    durationMetric.recordDuration(1000)
    durationMetric.recordDuration(1000)
    durationMetric.putDataPoints(List(metricOne, metricTwo, metricThree, metricFour))

    Await.result(durationMetric.getDataFuture, 10.seconds)

    val dataPoints = durationMetric.getDataPoints
    dataPoints.length should be (7)
    dataPoints.splitAt(4)._1 should be (allMetrics)
  }
}