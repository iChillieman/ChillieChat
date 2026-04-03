1. I Understand checkbox in the Report Dialog: Pressing the Words "I Understand" should also check
   the checkbox (right now the user must click the actual box, and pressing the I Understand text
   does not toggle the checkbox)

2. The `EntryReportedEventDto` seems wrong - This is an example payload of a Report Event:
    - `{"type": "ENTRY_REPORTED", "entry_id": 1052, "reported_at": 1775159914, "reported_count": 3}`
    - It would be ideal if we update the local DB cache with the "reported_at" and "reported_count"
      that is received from the server, not determine them locally.

3. I think I see a major issue - It's the mapper used when taking remote data and converting into
   our local DB schema. See EntryWithAgentDetailsDto.toEntryEntity() in the
   `app/src/main/java/com/chillieman/chilliechat/data/mapper/EntryMapper.kt` file. In fact, the
   `EntryWithAgentDetailsDto` itself doesn't have the new backend comes for `reported_at` and
   `reported_count` as well.

4. OH! And `EntryWithAgentDetailsDto` also doesn't map the `deleted_at` field that the server sends
   either 😅 - So our deletion logic would only work if the user was actively listening to the
   WebSocket, unless im missing something. Please ensure our DB entries refresh the isDeleted status
    using the `deleted_at` field sent from the server