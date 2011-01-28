(function ($) {


    function TagGroup(_id, _sortOrder) {
        var tagGroup = this;

        this.tags = new Array();
        this.id = _id;
        this.sortOrder = _sortOrder;

        this.addTag = function(tagElement) {
            tagGroup.tags.push(tagElement);
        }
    }

    function findTagGroupInList(list, tagGroupId) {
        for (var i in list) {
            if (list[i].id == tagGroupId) return list[i];
        }
        return null;
    }

    $.fn.tagGroups = function (options) {
        var defaults = {};

        var settings = $.extend({}, defaults, options);

        return this.each(function () {
            var tagGroupsList = new Array();
            
            var tagsElement = $(this);

            $(".tag", tagsElement).each(function() {
                var tagGroupId = $(this).attr("data-groupId");
                var tagGroupsortOrder = $(this).attr("data-groupSortOrder");

                var tagGroup = findTagGroupInList(tagGroupsList, tagGroupId);

                if (!tagGroup) {
                    tagGroup = new TagGroup(tagGroupId, tagGroupsortOrder);
                    tagGroupsList.push(tagGroup);
                }

                tagGroup.addTag($(this).detach());
            });

            // sort tagGroupsList
            tagGroupsList.sort(function(a, b) {
               return b.sortOrder - a.sortOrder;
            });

            for (var i in tagGroupsList) {
                var tagGroup = tagGroupsList[i];

                var tagGroupElement = $("<li class='tagGroup tagGroupOrder-" + tagGroup.sortOrder + "' data-id='" + tagGroup.id + "'/>");
                tagsElement.append(tagGroupElement);

                for (var j in tagGroup.tags) {
                    var tag = tagGroup.tags[j];
                    tag.appendTo(tagGroupElement);
                }
            }

        });
    };
})(jQuery);
