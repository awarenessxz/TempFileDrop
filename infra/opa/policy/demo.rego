contains(arr, elem) = true {
  arr[_] = elem
} else = false { true }

contains_key(map, elem) = true {
	map[elem]
} else = false { true }

# Define Attributes
attributes = {
	"tempfiledrop": {"buckets": ["tempfiledrop"], "routingkeys": ["tempfiledrop"]},
	"example": {"buckets": ["example", "example2"], "routingkeys": ["example"]}
}

permissions := ["storage|access", "storage-consumer|tempfiledrop", "storage-consumer|example", "storage|admin"]
storage_roles = [x | x := permissions[_]; startswith(x, "storage-consumer|")]		# BOTH WORKS
#storage_roles[x] { x := permissions[_]; startswith(x, "storage-consumer|") }		# BOTH WORKS
#storage_roles[x] {							# partial rules
#	x := permissions[_]						# some permission exists
#    startswith(x, "storage-consumer|")		# starts with storage-consumer |
#}

## Logical OR Condition
#default is_allowed = false
#is_allowed = true {							# is true if body
#	permissions[_] == "storage|access"		# storage|access exists
#}
#is_allowed = true {
#	permissions[_] == "storage|admin"
#}

## Logical AND condition
#is_admin = true {
#	permissions[_] == "storage|access"
#    permissions[_] == "storage|admin"
#} else = false

#buckets[role] = bucketAttr {
#	role := storage_roles[_]		# some permission
#    key := replace(role, "storage-consumer|", "")
#    contains_key(attributes, key)
#    bucketAttr := attributes[key].buckets
#}

# Merging multiple arrays into one using comprehension
#output = [x |
#	some k; 				# for some key 'k'
#    x := buckets[k][_] 		# x = element inside array
#]

# Retrieving Buckets and store into 1 single array
buckets = [x |
	r := storage_roles[_]						# some client role 'r' Eg. storage-consumer|tempfiledrop
    k := replace(r, "storage-consumer|", "")	# remove prefix 'storage-consumer|' and assign to 'k'
    contains_key(attributes, k)					# check if key exists in attributes
    b := attributes[k].buckets					# retrieve bucket attributes and assign it to 'b'
    some i										# some element index 'i', for looping array
    x := b[i]									# retrieve array value
]