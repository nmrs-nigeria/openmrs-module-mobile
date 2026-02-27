 

# Developer Guild

#### Compatibility History 
+ Client_tracking_and_discontinuation_form_24.1.4.json required mobile App version 24.1.4. -->Form views id rearrange to fit group hide and show. ChildConcept+RepeatedConcept to identify a child in a group in json.
+ Laboratory24.1.3.json   required mobile App version 24.1.3.  -->Radio box control show and hide.






#### Build JSON forms: 

```
{
    "name": "Form Name e.g Laboratory Order and Result Form", //displayed at App bar 
    "uuid": "The form UUID e.g 889ce948-f1ee-4656-91af-147a9e760309",
    "processor": "EncounterFormProcessor", // options: EncounterFormProcessor
    "pages": // Array of pages to be displayed.  
    [
        // section 1
        {
            "label": "Laboratory Order and Result Form", // sub header displayed imediately after app bar
            "sections": [
                {
                    "label": "Laboratory Order and Result Form", // section header
                    "questions": // Array of questions JSON object of a page  
                    [
                        // question 1
                        {
                            "label": "", // required. display label for the question
                            "required": "yes", //optional. values [yes or  no]   
                            "questionOptions": {
                                "rendering": "date", //    values [date, text, number, select,radio check]  
                                "concept": "6bcaf85b-8504-4c7f-b510-a50436236b80",
                                "max": "1000000000000", // only for rendering type number
                                "min": "0" // only for irendering type only for number
                            },
                            "type": "encounterDate", // values [encounterDate, obs]
                            "validators": [
                                {
                                    "type": "date" // only for date
                                    // todo
                                }
                            ],
                            "answers": // array of options to presented only for rendering type [ select, check and radio]
                            [
                                //option 1
                                {
                                    "concept": "162080AAAAAAAAAAAAAAAAAAAAAAAAAAAAAA", // value or concept uuid for the value
                                    "label": "option1"
                                },
                                //option 2
                                {
                                    "concept": "162081AAAAAAAAAAAAAAAAAAAAAAAAAAAAAA", // value or concept uuid for the value
                                    "label": "option2"
                                }
                            ],
                            "control": // only when answers key exist.  
                            {
                                "conditionOptions": [
                                    //Below question control.  
                                    {
                                        "when": "option1", // label  value of another question2
                                        "displayType": "show",
                                        "childControl": "34b689bd-eb24-4ad4-8642-fc3b4a9bdd44", // concept id of the question2
                                        "controlType": "text" //  rendering type of the question
                                    },
                                    {
                                        "when": "option2", // label  value of another question2
                                        "displayType": "hide",
                                        "childControl": "34b689bd-eb24-4ad4-8642-fc3b4a9bdd44", // concept id of the question2
                                        "controlType": "text" // rendering type of the questioon
                                    }
                                    // add other options for the question if exist
                                    //Add other  option for another question if needed
                                ]
                            }
                        }
                        // other questions
                    ]
                }
            ]
        }
        ///other section
        ,
        // section 2
        {
            "label": "Laboratory Order and Result Form", // sub header displayed imediately after app bar
            "sections": [
                {
                    "label": "Laboratory Order and Result Form", // section header
                    "questions": // Array of questions JSON object of a page  
                    [
                        // question  1 enter date 
                        {
                            "label": "Visit Date", // required. display label for the question
                            "required": "yes", //optional. values [yes or  no]   
                            "questionOptions": {
                                "rendering": "date", //       
                                "concept": "6bcaf85b-8504-4c7f-b510-a50436236b80"
                            },
                            "type": "encounterDate",
                            "validators": [
                                {
                                    "type": "date"
                                }
                            ]
                        },
                        // question 2 enter text 
                        {
                            "label": "Lab Registration Number", // required. display label for the question
                            // require automaticall No
                            "id": "lab_registration_number", // id 
                            "questionOptions": {
                                "rendering": "text", // input type
                                "concept": "f16ab53d-25f4-4e3b-b5e3-8adf650308cd"
                            },
                            "type": "obs",
                            "validators": []
                        },
                        // question 3 enter text 
                        {
                            "label": "Indication for AHD",
                            "id": "Indication_for_ahd",
                            "type": "obs",
                            "questionOptions": {
                                "rendering": "select", // [ select, check, radio ] works for question 3
                                "concept": "a80fd390-3c6d-4aab-b16e-e3a98c39cc5e",
                                "answers": // aray of options to presented
                                [
                                    {
                                        "concept": "162080AAAAAAAAAAAAAAAAAAAAAAAAAAAAAA", // value or concept uuid for the value
                                        "label": "Baseline"
                                    },
                                    {
                                        "concept": "162081AAAAAAAAAAAAAAAAAAAAAAAAAAAAAA", // value or concept uuid for the value
                                        "label": "Repeat"
                                    }
                                ],
                                "control": // to show on hide   question 4
                                {
                                    "conditionOptions": [
                                        {
                                            "when": "Baseline", // label  value of another question2
                                            "displayType": "show",
                                            "childControl": "1319AAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAA", // concept id of the question2
                                            "controlType": "number" //  rendering type of the question
                                        },
                                        {
                                            "when": "Repeat", // label  value of another question2
                                            "displayType": "hide",
                                            "childControl": "1319AAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAA", // concept id of the question2
                                            "controlType": "number" // rendering type of the questioon
                                        }
                                        // add other options for the question if exist
                                        //Add other  option for another question if needed
                                    ]
                                }
                            }
                        },
                        // question4  
                        {
                            "defaultDisplay": "hide", // make it Gone until an action set it to visbble
                            "label": "Lymphocytes (/mm^3):",
                            "id": "Lymphocytes",
                            "questionOptions": {
                                "rendering": "number",
                                "concept": "1319AAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAA",
                                "max": "350", // maximum value
                                "min": "0" // minimum vale 
                            },
                            "type": "obs",
                            "validators": []
                        },
                    ]
                }
            ]
        },
        // other pages 
    ]
}
```


this.concept = concept;
        this.id = Math.abs((concept + repeat).hashCode());

# License
This project is licensed under the OpenMRS Public License, see the [copyright](copyright/copyright) file for details. 
