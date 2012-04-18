window.onBodyLoad = ->
  document.addEventListener("deviceready", onDeviceReady, false)

window.onDeviceReady = ->
  Cordova.exec(
    onTNReady, null, 'cordovatruenative.component', 'loadJavascript', [])

onTNReady = ->
  navController = new TN.UI.NavigationController
  navController.push(new TN.UI.Window(
    title: "True Native"
    constructView: (view) ->
      entries = []

      # Adds a row object to the entries array. Each object in the entries
      # array corresponds to one row of the table view.
      addExample = (name, windowCallback) ->
        entries.push(
          userData:
            exampleName: name
            windowCallback: windowCallback
        )

      addExample('Action Sheet', App.createActionSheetDemoWindow)
      addExample('Instagram', App.createInstagramDemoWindow)
      addExample('Twitter', App.createTwitterDemoWindow)

      tableView = new TN.UI.TableView(entries: entries)

      constructRow = (rowEntry, row) ->
        row.setProperty('hasDetail', true)
        row.addEventListener('click', ->
          navController.push(row.userData.windowCallback(navController))
        )

      reuseRow = (rowEntry, row) ->
        row.setProperty('text', rowEntry.userData.exampleName)
        
        # Save the window for the click handler.
        row.userData.windowCallback = rowEntry.userData.windowCallback

      # Each row object specifies a template name. All rows here use the same
      # template name. This tells the table view about the template.
      tableView.addRowTemplate(
        constructCallback: constructRow
        reuseCallback: reuseRow
      )

      # Add the table view to the window's view and stretch it to fill the
      # window.
      TN.glueViews(view, tableView)
  ))
  navController.open()

  navController.push(App.createTwitterDemoWindow())
