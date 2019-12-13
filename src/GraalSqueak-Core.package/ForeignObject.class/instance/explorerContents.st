explorer
explorerContents
	^ (self explorableMembers asOrderedCollection withIndexCollect: [:member :index |
		ObjectExplorerWrapper
			with: (Interop readMember: self member: member)
			name: member
			model: self]) ,
		((1 to: self basicSize) collect: [:index |
			ObjectExplorerWrapper
				with: (self basicAt: index)
				name: index printString
				model: self ])