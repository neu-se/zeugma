import html

import tables


def choose_representatives(failures):
    # Take the first detecting campaign for each unique failure
    unique_failures = failures.groupby(['subject', 'type', 'trace'], group_keys=True)[
        ['fuzzer', 'inducing_inputs', 'campaign_id']] \
        .apply(lambda x: x.sort_values('fuzzer', ascending=False).iloc[0]) \
        .sort_index() \
        .reset_index()
    # Take the first inducing input list for the selected campaign for each failure
    unique_failures['inducing_input'] = unique_failures['inducing_inputs'] \
        .apply(lambda x: x[0]) \
        .apply(lambda x: x[x.index('/meringue/campaign/') + len('/meringue/campaign/'):])
    return unique_failures.drop(columns=['inducing_inputs'])


def style_failures(failures):
    return failures.style \
        .format_index(axis=1, formatter=lambda x: x.replace('_', ' ').title()) \
        .format({'trace': lambda x: "<br>".join(f"{html.escape(repr(y))}" for y in x)}) \
        .set_table_attributes('class="data-table"') \
        .set_caption("Unmatched Failures")


def create(campaigns, times):
    print('Creating defects section.')
    failures = tables.create_failures_table(campaigns)
    unmatched = failures[failures['associatedDefects'].isnull()]
    # Report an arbitrary, failure-inducing input for each unique, unmatched failure
    unmatched_reps = choose_representatives(unmatched)
    content = style_failures(unmatched_reps).to_html()
    # Compute detection rates for failures matching known failures
    defects = tables.create_defect_table(campaigns, times)
    content += tables.style_table(defects, precision=2, axis=0) \
        .set_caption('Detection Rates') \
        .set_table_attributes('class="data-table"') \
        .to_html()
    print(f'\tCreated defects section.')
    return f'<div id="defects"><h2>Defects</h2>{content}</div>'
