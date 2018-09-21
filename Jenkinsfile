/*
 * Copyright (C) 2007-2018, GoodData(R) Corporation. All rights reserved.
 */

@Library('pipelines-shared-libs')
import com.gooddata.pipeline.Pipeline

def config = [
    'microservices': [
        'graphene-tests': [
            'docker': [
                'dockerfile': './Dockerfile',
                'customTags': [ '67.0.3396.79-2.39' ]
            ]
        ]
    ]
]

Pipeline.get(this, config).run()
