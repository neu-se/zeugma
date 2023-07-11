# Data

Raw data for the evaluation of Zeugma.

## RQ1: Heritability

The file `heritability.csv` contains inheritance rates and hybrid data for the heritability experiment.
Each row reports results for a single child input and indicates the fuzzing subject that the input was created for, the
type of crossover used to produce the child, the inheritance rate of the child and whether the child was hybrid.
Consider the row:

```
subject,crossover_operator,inheritance_rate,hybrid
...
Rhino,Linked,0.817073,false
```

This row indicates that a child for `Rhino` created using `Linked` crossover had an inheritance rate of `0.817073` and
was not a hybrid.
There are 1,000 rows for each crossover operator for each subject.
We used these entries to compute the heritability metrics for the crossover operators.

The file ``saved_inputs.csv`` contains data about the number of inputs that were saved to the corpus of campaigns for
Zeugma-X.
Each row indicates the identifier of the campaign, the subject the campaign was performed on, the total number of inputs
saved over the full 3 hours of the campaign, and the number of inputs saved in the first 5 minutes of the campaign.
Consider the row:

```
campaign_id,subject,total_saved,saved_5m
...
264,Rhino,530,332
```

This row indicates that the campaign with the unique identifier `264` which was performed on `Rhino` saved a total of
`530` inputs `332` of which were saved in the first 5 minutes of the campaign.
We used this data to determine that on average over half of the inputs saved to corpus at the end of the three-hour
campaign were saved in the first five minutes.

## RQ2: Coverage

The file `coverage.csv` contains JaCoCo branch coverage measurements for the fuzzing campaigns.
Each row indicates the branch coverage recorded for a particular campaign at a certain time.
Note that the measurement at 0 seconds is normally non-zero because Meringue computes times relative to the first saved
input.
Times are in the format D days HH:MM:SS[.UUUUUUUUU] where D is days, HH is hours, MM is minutes, SS is seconds, and
UUUUUUUUU is nanoseconds.

Consider the row:

```
campaign_id,fuzzer,subject,time,covered_branches
...
0,BeDiv-Simple,Ant,0 days 00:05:00,732
```

This row indicates that the campaign with the unique identifier `0` which was performed on `Ant` using the
fuzzer `BeDiv-Simple` covered `732` branches by time `0 days 00:05:00`.

There are 1,001 measurements for each campaign: 1,000 measurements at equal time intervals starting at 0 seconds and
ending at 3 hours, and 1 measurement at 5 minutes.
We used the measurements at 5 minutes and 3 hours to evaluate coverage for short and long campaigns.
We used all the measurements to plot coverage over time.

## RQ3: Defects
The file `defects.json` TODO

The file `failures.json` TODO

The file `detection_times.csv` TODO
